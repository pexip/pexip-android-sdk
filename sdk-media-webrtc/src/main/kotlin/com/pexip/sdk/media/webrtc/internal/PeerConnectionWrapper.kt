/*
 * Copyright 2023 Pexip AS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.pexip.sdk.media.webrtc.internal

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.webrtc.AddIceObserver
import org.webrtc.DataChannel
import org.webrtc.IceCandidate
import org.webrtc.MediaStreamTrack.MediaType
import org.webrtc.PeerConnection
import org.webrtc.PeerConnection.RTCConfiguration
import org.webrtc.PeerConnectionFactory
import org.webrtc.RtpTransceiver
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription
import org.webrtc.VideoTrack
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlin.properties.Delegates

internal class PeerConnectionWrapper(
    private val factory: PeerConnectionFactory,
    private val rtcConfig: RTCConfiguration,
    private val init: DataChannel.Init?,
) {

    private val mutex = Mutex()
    private val observer = Observer(rtcConfig.continualGatheringPolicy)
    private var peerConnection by Delegates.observable<PeerConnection?>(null) { _, old, _ ->
        old?.dispose()
    }
    private var dataChannel by Delegates.observable<DataChannel?>(null) { _, old, new ->
        if (old != null) {
            old.unregisterObserver()
            old.close()
            old.dispose()
        }
        new?.registerObserver(observer)
    }
    private val rtpTransceivers = mutableMapOf<RtpTransceiverKey, RtpTransceiver>()
    private val iceCredentials = mutableMapOf<String, IceCredentials>()
    private val localFingerprints = MutableStateFlow(emptyList<String>())
    private val remoteFingerprints = MutableStateFlow(emptyList<String>())
    private val remoteIceCandidates = mutableListOf<IceCandidate>()
    private val restart = MutableSharedFlow<Unit>()

    val secureCheckCode = localFingerprints.combine(remoteFingerprints, ::SecureCheckCode)

    val event get() = observer.event

    init {
        peerConnection = checkNotNull(factory.createPeerConnection(rtcConfig, observer))
        dataChannel = when (init) {
            null -> null
            else -> peerConnection!!.createDataChannel("pexChannel", init)
        }
    }

    suspend fun start() = mutex.withLock {
        observer.start()
    }

    suspend fun restart() = mutex.withLock {
        observer.stop()
        restart.emit(Unit)
        localFingerprints.value = emptyList()
        remoteFingerprints.value = emptyList()
        remoteIceCandidates.clear()
        val newPeerConnection = checkNotNull(factory.createPeerConnection(rtcConfig, observer))
        val newDataChannel = when (init) {
            null -> null
            else -> newPeerConnection.createDataChannel("pexChannel", init)
        }
        val newRtpTransceivers = rtpTransceivers.mapValues { (key, t) ->
            val init = RtpTransceiverInit(
                direction = t.direction,
                streamIds = key.streamIds,
                sendEncodings = key.sendEncodings,
            )
            val transceiver = newPeerConnection.addTransceiver(key.mediaType, init)
            transceiver.sender.streams = t.sender.streams
            transceiver.sender.parameters = t.sender.parameters
            transceiver.sender.setTrack(t.sender.track(), false)
            t.sender.setTrack(null, false)
            transceiver
        }
        rtpTransceivers.clear()
        rtpTransceivers.putAll(newRtpTransceivers)
        dataChannel = newDataChannel
        peerConnection = newPeerConnection
        observer.start()
    }

    fun getRemoteVideoTrack(key: RtpTransceiverKey): Flow<VideoTrack?> {
        require(key.mediaType == MediaType.MEDIA_TYPE_VIDEO) { "Illegal mediaType: ${key.mediaType}." }
        val flow = channelFlow {
            val rtpTransceiver = mutex.withLock { rtpTransceivers[key] }
            val track = when (rtpTransceiver?.direction) {
                RtpTransceiver.RtpTransceiverDirection.RECV_ONLY -> rtpTransceiver.receiver.track()
                RtpTransceiver.RtpTransceiverDirection.SEND_RECV -> rtpTransceiver.receiver.track()
                else -> null
            }
            send(track as? VideoTrack)

            suspend fun predicate(owner: RtpReceiverOwner) = mutex.withLock {
                owner.receiver.id() == rtpTransceivers[key]?.receiver?.id()
            }

            val flow = merge(
                restart.map { null },
                event.filterIsInstance<Event.OnAddTrack>()
                    .filter(::predicate)
                    .map { it.receiver.track() as VideoTrack },
                event.filterIsInstance<Event.OnRemoveTrack>()
                    .filter(::predicate)
                    .map { null },
            )
            flow.collect(::send)
        }
        return flow.onCompletion { emit(null) }
    }

    suspend fun <T> withRtpTransceiver(
        key: RtpTransceiverKey,
        init: ((RtpTransceiverKey) -> RtpTransceiver.RtpTransceiverInit)? = null,
        block: (RtpTransceiver?) -> T,
    ): T = mutex.withLock {
        val rtpTransceiver = when (init) {
            null -> rtpTransceivers[key]
            else -> rtpTransceivers.getOrPut(key) {
                peerConnection!!.addTransceiver(key.mediaType, init(key))
            }
        }
        block(rtpTransceiver)
    }

    suspend fun syncRtpTransceiver(transceiver: RtpTransceiver) = mutex.withLock {
        val (key, t) = rtpTransceivers.entries
            .find { (_, t) -> t.mid == null && t.mediaType == transceiver.mediaType }
            ?: return@withLock
        transceiver.direction = t.direction
        transceiver.sender.streams = t.sender.streams
        transceiver.sender.parameters = t.sender.parameters
        transceiver.sender.setTrack(t.sender.track(), false)
        t.sender.setTrack(null, false)
        rtpTransceivers[key] = transceiver
        t.stopInternal()
    }

    suspend fun setLocalDescription(block: SessionDescription.(Map<RtpTransceiverKey, String>) -> SessionDescription = { this }): SessionDescription =
        mutex.withLock {
            peerConnection!!.setLocalDescription()
            iceCredentials.clear()
            val description = peerConnection!!.localDescription
            var ufrag: String? = null
            var pwd: String? = null
            var mid: String? = null
            val lines = description.splitToLineSequence()
            val fingerprints = mutableListOf<String>()
            for (line in lines) {
                if (line.startsWith(FINGERPRINT)) fingerprints += line.removePrefix(FINGERPRINT)
                if (line.startsWith(ICE_UFRAG)) ufrag = line.removePrefix(ICE_UFRAG)
                if (line.startsWith(ICE_PWD)) pwd = line.removePrefix(ICE_PWD)
                if (line.startsWith(MID)) mid = line.removePrefix(MID)
                if (ufrag != null && pwd != null && mid != null) {
                    iceCredentials[mid] = IceCredentials(ufrag, pwd)
                    ufrag = null
                    pwd = null
                    mid = null
                }
            }
            val mids = rtpTransceivers.mapValues { (_, rtpTransceiver) -> rtpTransceiver.mid }
            localFingerprints.value = fingerprints.toList()
            description.block(mids)
        }

    suspend fun setRemoteDescription(description: SessionDescription) = mutex.withLock {
        peerConnection!!.setRemoteDescription(description)
        remoteIceCandidates.forEach { peerConnection!!.doAddIceCandidate(it) }
        remoteIceCandidates.clear()
        remoteFingerprints.value = description.splitToLineSequence()
            .filter { it.startsWith(FINGERPRINT) }
            .map { it.removePrefix(FINGERPRINT) }
            .toList()
    }

    suspend fun addIceCandidate(candidate: IceCandidate) = mutex.withLock {
        if (peerConnection!!.remoteDescription == null) {
            remoteIceCandidates += candidate
        } else {
            peerConnection!!.doAddIceCandidate(candidate)
        }
    }

    suspend fun restartIce() = mutex.withLock { peerConnection!!.restartIce() }

    suspend fun getIceCredentials(mid: String) = mutex.withLock { iceCredentials[mid] }

    suspend fun dispose() = mutex.withLock {
        observer.stop()
        rtpTransceivers.forEach { (_, rtpTransceiver) ->
            rtpTransceiver.sender.setTrack(null, false)
        }
        rtpTransceivers.clear()
        iceCredentials.clear()
        dataChannel = null
        peerConnection = null
    }

    private suspend fun PeerConnection.setLocalDescription() =
        suspendCoroutine { setLocalDescription(SdpObserver(it)) }

    private suspend fun PeerConnection.setRemoteDescription(description: SessionDescription) =
        suspendCoroutine { setRemoteDescription(SdpObserver(it), description) }

    private suspend fun PeerConnection.doAddIceCandidate(candidate: IceCandidate) =
        suspendCoroutine {
            val observer = object : AddIceObserver {

                override fun onAddSuccess() = it.resume(Unit)

                override fun onAddFailure(reason: String) =
                    it.resumeWithException(RuntimeException(reason))
            }
            addIceCandidate(candidate, observer)
        }

    private fun SdpObserver(continuation: Continuation<Unit>): SdpObserver = object : SdpObserver {

        override fun onCreateSuccess(description: SessionDescription) = Unit

        override fun onCreateFailure(reason: String) = Unit

        override fun onSetSuccess() = continuation.resume(Unit)

        override fun onSetFailure(reason: String) =
            continuation.resumeWithException(RuntimeException(reason))
    }

    companion object {

        private const val FINGERPRINT = "a=fingerprint:"
        private const val MID = "a=mid:"
        private const val ICE_UFRAG = "a=ice-ufrag:"
        private const val ICE_PWD = "a=ice-pwd:"
    }
}
