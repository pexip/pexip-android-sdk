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

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.withContext
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

@OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
internal class PeerConnectionWrapper(factory: PeerConnectionFactory, rtcConfig: RTCConfiguration) {

    private val observer = PeerConnectionObserver()
    private val peerConnectionDelegate = lazy(LazyThreadSafetyMode.NONE) {
        checkNotNull(factory.createPeerConnection(rtcConfig, observer))
    }
    private val peerConnection by peerConnectionDelegate
    private val dispatcher = newSingleThreadContext("PeerConnection")
    private val rtpTransceivers = mutableMapOf<RtpTransceiverKey, RtpTransceiver>()
    private val iceCredentials = mutableMapOf<String, IceCredentials>()

    val event get() = observer.event

    fun getRemoteVideoTrack(key: RtpTransceiverKey): Flow<VideoTrack?> {
        require(key.mediaType == MediaType.MEDIA_TYPE_VIDEO) { "Illegal mediaType: ${key.mediaType}." }
        val flow = channelFlow {
            val rtpTransceiver = rtpTransceivers[key]
            val track = when (rtpTransceiver?.direction) {
                RtpTransceiver.RtpTransceiverDirection.RECV_ONLY -> rtpTransceiver.receiver.track()
                RtpTransceiver.RtpTransceiverDirection.SEND_RECV -> rtpTransceiver.receiver.track()
                else -> null
            }
            send(track as? VideoTrack)
            val predicate = { owner: RtpReceiverOwner ->
                owner.receiver.id() == rtpTransceivers[key]?.receiver?.id()
            }
            val flow = merge(
                event.filterIsInstance<Event.OnAddTrack>()
                    .filter(predicate)
                    .map { it.receiver.track() as VideoTrack },
                event.filterIsInstance<Event.OnRemoveTrack>()
                    .filter(predicate)
                    .map { null },
            )
            flow.collect(::send)
        }
        return flow
            .onCompletion { emit(null) }
            .flowOn(dispatcher)
    }

    suspend fun <T> withRtpTransceiver(
        key: RtpTransceiverKey,
        init: ((RtpTransceiverKey) -> RtpTransceiver.RtpTransceiverInit)? = null,
        block: (RtpTransceiver?) -> T,
    ): T = withContext(dispatcher) {
        val rtpTransceiver = when (init) {
            null -> rtpTransceivers[key]
            else -> rtpTransceivers.getOrPut(key) {
                peerConnection.addTransceiver(key.mediaType, init(key))
            }
        }
        block(rtpTransceiver)
    }

    suspend fun setLocalDescription(block: SessionDescription.(Map<RtpTransceiverKey, String>) -> SessionDescription = { this }): SessionDescription =
        withContext(dispatcher) {
            peerConnection.setLocalDescription()
            iceCredentials.clear()
            val localDescription = peerConnection.localDescription
            var ufrag: String? = null
            var pwd: String? = null
            var mid: String? = null
            val lines = localDescription.splitToLineSequence()
            for (line in lines) {
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
            localDescription.block(mids)
        }

    suspend fun setRemoteDescription(description: SessionDescription) =
        withContext(dispatcher) { peerConnection.setRemoteDescription(description) }

    suspend fun restartIce() = withContext(dispatcher) { peerConnection.restartIce() }

    suspend fun getIceCredentials(candidate: IceCandidate): IceCredentials? =
        withContext(dispatcher) { candidate.sdpMid?.let(iceCredentials::get) }

    suspend fun dispose() = dispatcher.use {
        withContext(it) {
            rtpTransceivers.forEach { (_, transceiver) ->
                transceiver.sender.setTrack(null, false)
            }
            rtpTransceivers.clear()
            iceCredentials.clear()
            if (peerConnectionDelegate.isInitialized()) {
                peerConnection.dispose()
            }
        }
    }

    private suspend fun PeerConnection.setLocalDescription() =
        suspendCoroutine { setLocalDescription(SdpObserver(it)) }

    private suspend fun PeerConnection.setRemoteDescription(description: SessionDescription) =
        suspendCoroutine { setRemoteDescription(SdpObserver(it), description) }

    private fun SdpObserver(continuation: Continuation<Unit>): SdpObserver = object : SdpObserver {

        override fun onCreateSuccess(description: SessionDescription) = Unit

        override fun onCreateFailure(reason: String) = Unit

        override fun onSetSuccess() = continuation.resume(Unit)

        override fun onSetFailure(reason: String) =
            continuation.resumeWithException(RuntimeException(reason))
    }

    companion object {

        private const val MID = "a=mid:"
        private const val ICE_UFRAG = "a=ice-ufrag:"
        private const val ICE_PWD = "a=ice-pwd:"
    }
}