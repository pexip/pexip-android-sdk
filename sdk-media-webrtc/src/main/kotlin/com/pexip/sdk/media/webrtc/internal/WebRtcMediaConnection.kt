/*
 * Copyright 2022-2023 Pexip AS
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

import com.pexip.sdk.media.Bitrate
import com.pexip.sdk.media.Bitrate.Companion.bps
import com.pexip.sdk.media.DegradationPreference
import com.pexip.sdk.media.LocalAudioTrack
import com.pexip.sdk.media.LocalMediaTrack
import com.pexip.sdk.media.LocalVideoTrack
import com.pexip.sdk.media.MediaConnection
import com.pexip.sdk.media.MediaConnectionConfig
import com.pexip.sdk.media.webrtc.WebRtcMediaConnectionFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.webrtc.IceCandidate
import org.webrtc.MediaStream
import org.webrtc.MediaStreamTrack.MediaType
import org.webrtc.PeerConnection
import org.webrtc.RtpReceiver
import org.webrtc.RtpTransceiver
import org.webrtc.RtpTransceiver.RtpTransceiverDirection
import org.webrtc.SessionDescription
import java.util.concurrent.CopyOnWriteArraySet
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.properties.Delegates

internal class WebRtcMediaConnection(
    factory: WebRtcMediaConnectionFactory,
    private val config: MediaConnectionConfig,
) : MediaConnection, SimplePeerConnectionObserver {

    private val started = AtomicBoolean()
    private val shouldAck = AtomicBoolean(true)
    private val shouldRenegotiate = AtomicBoolean()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val dispatcher = Dispatchers.IO.limitedParallelism(1)
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)
    private val iceCredentials = mutableMapOf<String, IceCredentials>()
    private val connection = factory.createPeerConnection(createRTCConfiguration(), this)

    private var bitrate by Delegates.observable(0.bps) { _, old, new ->
        if (old != new) connection.restartIce()
    }

    // Rename due to platform declaration clash
    @set:JvmName("setMainDegradationPreferenceInternal")
    private var mainDegradationPreference by Delegates.observable(DegradationPreference.BALANCED) { _, old, new ->
        if (old == new) return@observable
        synchronized(mainVideoTransceiverLock) {
            mainVideoTransceiver?.setDegradationPreference(new)
        }
    }

    // Rename due to platform declaration clash
    @set:JvmName("setPresentationDegradationPreferenceInternal")
    private var presentationDegradationPreference by Delegates.observable(DegradationPreference.BALANCED) { _, old, new ->
        if (old != new) return@observable
        synchronized(presentationVideoTransceiver) {
            presentationVideoTransceiver.setDegradationPreference(new)
        }
    }

    // Rename due to platform declaration clash
    @set:JvmName("setMainAudioTrackInternal")
    private var mainAudioTrack: LocalAudioTrack? by Delegates.observable(null) { _, old, new ->
        if (started.get() && old != new && new != null) onMainAudioCapturingChange(new.capturing)
        old?.unregisterCapturingListener(mainAudioTrackCapturingListener)
        new?.registerCapturingListener(mainAudioTrackCapturingListener)
    }

    // Rename due to platform declaration clash
    @set:JvmName("setMainVideoTrackInternal")
    private var mainVideoTrack: LocalVideoTrack? by Delegates.observable(null) { _, old, new ->
        if (started.get() && old != new && new != null) onMainVideoCapturingChange(new.capturing)
        old?.unregisterCapturingListener(mainVideoTrackCapturingListener)
        new?.registerCapturingListener(mainVideoTrackCapturingListener)
    }

    // Rename due to platform declaration clash
    @set:JvmName("setPresentationVideoTrackInternal")
    private var presentationVideoTrack: LocalVideoTrack? by Delegates.observable(null) { _, old, new ->
        if (old == new) return@observable
        if (new != null) onTakeFloor() else onReleaseFloor()
    }

    private var mainAudioTransceiver: RtpTransceiver? = null
    private var mainVideoTransceiver: RtpTransceiver? = null
    private val presentationVideoTransceiver: RtpTransceiver = connection.addTransceiver(
        MediaType.MEDIA_TYPE_VIDEO,
        RtpTransceiverInit(
            direction = RtpTransceiverDirection.INACTIVE,
            sendEncodings = listOf(Encoding { maxFramerate = MAX_FRAMERATE }),
        ),
    )
    private val mainAudioTransceiverLock = Any()
    private val mainVideoTransceiverLock = Any()
    private val mainRemoteVideoTrackListeners =
        CopyOnWriteArraySet<MediaConnection.RemoteVideoTrackListener>()
    private val presentationRemoteVideoTrackListeners =
        CopyOnWriteArraySet<MediaConnection.RemoteVideoTrackListener>()
    private val mainAudioTrackCapturingListener = LocalMediaTrack.CapturingListener {
        if (started.get()) onMainAudioCapturingChange(it)
    }
    private val mainVideoTrackCapturingListener = LocalMediaTrack.CapturingListener {
        if (started.get()) onMainVideoCapturingChange(it)
    }

    override val mainRemoteVideoTrack: com.pexip.sdk.media.VideoTrack?
        get() = synchronized(mainVideoTransceiverLock) {
            mainVideoTransceiver?.takeIf { scope.isActive }
                ?.takeIf { it.direction == RtpTransceiverDirection.SEND_RECV }
                ?.receiver
                ?.videoTrack
                ?.let(::WebRtcVideoTrack)
        }

    override val presentationRemoteVideoTrack: com.pexip.sdk.media.VideoTrack?
        get() = synchronized(presentationVideoTransceiver) {
            presentationVideoTransceiver.takeIf { scope.isActive }
                ?.takeIf { it.direction == RtpTransceiverDirection.RECV_ONLY || it.direction == RtpTransceiverDirection.SEND_RECV }
                ?.receiver
                ?.videoTrack
                ?.let(::WebRtcVideoTrack)
        }

    init {
        presentationVideoTransceiver.setDegradationPreference(presentationDegradationPreference)
        scope.launch {
            try {
                awaitCancellation()
            } finally {
                withContext(NonCancellable) {
                    mainAudioTrack = null
                    mainVideoTrack = null
                    presentationVideoTrack = null
                    synchronized(mainAudioTransceiverLock) {
                        mainAudioTransceiver?.sender?.setTrack(null, false)
                    }
                    synchronized(mainVideoTransceiverLock) {
                        mainVideoTransceiver?.sender?.setTrack(null, false)
                    }
                    synchronized(presentationVideoTransceiver) {
                        presentationVideoTransceiver.sender.setTrack(null, false)
                    }
                    mainRemoteVideoTrackListeners.clear()
                    presentationRemoteVideoTrackListeners.clear()
                    connection.dispose()
                }
            }
        }
    }

    override fun setMainAudioTrack(localAudioTrack: LocalAudioTrack?) {
        val lat = when (localAudioTrack) {
            is WebRtcLocalAudioTrack -> localAudioTrack
            null -> null
            else -> throw IllegalArgumentException("localAudioTrack must be null or an instance of WebRtcLocalAudioTrack.")
        }
        scope.launch {
            synchronized(mainAudioTransceiverLock) {
                val t = mainAudioTransceiver ?: connection.maybeAddTransceiver(lat)
                t?.maybeSetNewDirection(lat)
                t?.setTrack(lat)
                mainAudioTransceiver = t
            }
            mainAudioTrack = lat
        }
    }

    override fun setMainVideoTrack(localVideoTrack: LocalVideoTrack?) {
        val lvt = when (localVideoTrack) {
            is WebRtcLocalVideoTrack -> localVideoTrack
            null -> null
            else -> throw IllegalArgumentException("localVideoTrack must be null or an instance of WebRtcLocalVideoTrack.")
        }
        scope.launch {
            synchronized(mainVideoTransceiverLock) {
                val t = mainVideoTransceiver ?: connection.maybeAddTransceiver(lvt)?.apply {
                    setDegradationPreference(mainDegradationPreference)
                }
                t?.maybeSetNewDirection(lvt)
                t?.setTrack(lvt)
                mainVideoTransceiver = t
            }
            mainVideoTrack = lvt
        }
    }

    override fun setPresentationVideoTrack(localVideoTrack: LocalVideoTrack?) {
        val lvt = when (localVideoTrack) {
            is WebRtcLocalVideoTrack -> localVideoTrack
            null -> null
            else -> throw IllegalArgumentException("localVideoTrack must be null or an instance of WebRtcLocalVideoTrack.")
        }
        scope.launch {
            synchronized(presentationVideoTransceiver) {
                presentationVideoTransceiver.maybeSetNewDirection(lvt)
                presentationVideoTransceiver.setTrack(lvt)
            }
            presentationVideoTrack = lvt
        }
    }

    override fun setMainRemoteAudioTrackEnabled(enabled: Boolean) {
        scope.launch {
            synchronized(mainAudioTransceiverLock) {
                mainAudioTransceiver = mainAudioTransceiver ?: connection.maybeAddTransceiver(
                    mediaType = MediaType.MEDIA_TYPE_AUDIO,
                    receive = enabled,
                )
                mainAudioTransceiver?.maybeSetNewDirection(enabled)
            }
        }
    }

    override fun setMainRemoteVideoTrackEnabled(enabled: Boolean) {
        scope.launch {
            synchronized(mainVideoTransceiverLock) {
                mainVideoTransceiver = mainVideoTransceiver ?: connection.maybeAddTransceiver(
                    mediaType = MediaType.MEDIA_TYPE_VIDEO,
                    receive = enabled,
                )
                mainVideoTransceiver?.maybeSetNewDirection(enabled)
            }
        }
    }

    override fun setPresentationRemoteVideoTrackEnabled(enabled: Boolean) {
        if (!config.presentationInMain) {
            scope.launch {
                synchronized(presentationVideoTransceiver) {
                    presentationVideoTransceiver.maybeSetNewDirection(enabled)
                }
            }
        }
    }

    override fun setMaxBitrate(bitrate: Bitrate) {
        scope.launch {
            this@WebRtcMediaConnection.bitrate = bitrate
        }
    }

    override fun setMainDegradationPreference(preference: DegradationPreference) {
        scope.launch {
            mainDegradationPreference = preference
        }
    }

    override fun setPresentationDegradationPreference(preference: DegradationPreference) {
        scope.launch {
            presentationDegradationPreference = preference
        }
    }

    @Deprecated(
        message = "Use setPresentationVideoReceive(true) instead.",
        replaceWith = ReplaceWith("setPresentationVideoReceive(true)"),
    )
    override fun startPresentationReceive() = setPresentationRemoteVideoTrackEnabled(true)

    @Deprecated(
        message = "Use setPresentationVideoReceive(false) instead.",
        replaceWith = ReplaceWith("setPresentationVideoReceive(false)"),
    )
    override fun stopPresentationReceive() = setPresentationRemoteVideoTrackEnabled(false)

    override fun dtmf(digits: String) {
        scope.launch {
            runCatching { config.signaling.onDtmf(digits) }
        }
    }

    override fun start() {
        if (started.compareAndSet(false, true)) {
            setLocalDescription()
        }
    }

    override fun dispose() {
        scope.cancel()
    }

    override fun registerMainRemoteVideoTrackListener(listener: MediaConnection.RemoteVideoTrackListener) {
        mainRemoteVideoTrackListeners += listener
    }

    override fun unregisterMainRemoteVideoTrackListener(listener: MediaConnection.RemoteVideoTrackListener) {
        mainRemoteVideoTrackListeners -= listener
    }

    override fun registerPresentationRemoteVideoTrackListener(listener: MediaConnection.RemoteVideoTrackListener) {
        presentationRemoteVideoTrackListeners += listener
    }

    override fun unregisterPresentationRemoteVideoTrackListener(listener: MediaConnection.RemoteVideoTrackListener) {
        presentationRemoteVideoTrackListeners -= listener
    }

    override fun onStandardizedIceConnectionChange(newState: PeerConnection.IceConnectionState) {
        if (newState != PeerConnection.IceConnectionState.FAILED) return
        scope.launch {
            connection.restartIce()
        }
    }

    override fun onIceCandidate(candidate: IceCandidate) {
        scope.launch {
            val mid = candidate.sdpMid ?: return@launch
            val (ufrag, pwd) = iceCredentials[mid] ?: return@launch
            runCatching {
                config.signaling.onCandidate(
                    candidate = candidate.sdp,
                    mid = mid,
                    ufrag = ufrag,
                    pwd = pwd,
                )
            }
        }
    }

    override fun onRenegotiationNeeded() {
        // Skip the first call to onRenegotiationNeeded() since it's called right after
        // PeerConnection creation and we're still not ready to use setLocalDescription()
        if (shouldRenegotiate.compareAndSet(false, true)) return
        setLocalDescription()
    }

    override fun onAddTrack(receiver: RtpReceiver, streams: Array<out MediaStream>) {
        val id = receiver.id()
        val videoTrack = receiver.videoTrack?.let(::WebRtcVideoTrack)
        scope.launch {
            when (id) {
                synchronized(mainVideoTransceiverLock) { mainVideoTransceiver?.receiver?.id() } -> {
                    mainRemoteVideoTrackListeners.notify(videoTrack)
                }
                synchronized(presentationVideoTransceiver) { presentationVideoTransceiver.receiver.id() } -> {
                    presentationRemoteVideoTrackListeners.notify(videoTrack)
                }
            }
        }
    }

    override fun onRemoveTrack(receiver: RtpReceiver) {
        val id = receiver.id()
        scope.launch {
            when (id) {
                synchronized(mainVideoTransceiverLock) { mainVideoTransceiver?.receiver?.id() } -> {
                    mainRemoteVideoTrackListeners.notify(null)
                }
                synchronized(presentationVideoTransceiver) { presentationVideoTransceiver.receiver.id() } -> {
                    presentationRemoteVideoTrackListeners.notify(null)
                }
            }
        }
    }

    private fun setLocalDescription() {
        scope.launch {
            connection.setLocalDescription()
            val result = connection.localDescription.mangle(
                bitrate = bitrate,
                mainAudioMid = synchronized(mainAudioTransceiverLock) {
                    mainAudioTransceiver?.mid
                },
                mainVideoMid = synchronized(mainVideoTransceiverLock) {
                    mainVideoTransceiver?.mid
                },
                presentationVideoMid = synchronized(presentationVideoTransceiver) {
                    presentationVideoTransceiver.mid
                },
            )
            iceCredentials.clear()
            iceCredentials.putAll(result.iceCredentials)
            val sdp = SessionDescription(
                SessionDescription.Type.ANSWER,
                config.signaling.onOffer(
                    callType = "WEBRTC",
                    description = result.description.description,
                    presentationInMain = config.presentationInMain,
                    fecc = config.farEndCameraControl,
                ),
            )
            connection.setRemoteDescription(sdp.mangle(bitrate))
            if (shouldAck.compareAndSet(true, false)) {
                runCatching { config.signaling.onAck() }
                mainAudioTrack?.let { onMainAudioCapturingChange(it.capturing) }
                mainVideoTrack?.let { onMainVideoCapturingChange(it.capturing) }
            }
        }
    }

    private fun onMainAudioCapturingChange(capturing: Boolean) {
        scope.launch {
            runCatching {
                with(config.signaling) { if (capturing) onAudioUnmuted() else onAudioMuted() }
            }
        }
    }

    private fun onMainVideoCapturingChange(capturing: Boolean) {
        scope.launch {
            runCatching {
                with(config.signaling) { if (capturing) onVideoUnmuted() else onVideoMuted() }
            }
        }
    }

    private suspend fun Collection<MediaConnection.RemoteVideoTrackListener>.notify(videoTrack: WebRtcVideoTrack?) {
        withContext(Dispatchers.Main) {
            forEach {
                it.safeOnRemoteVideoTrack(videoTrack)
            }
        }
    }

    private fun onTakeFloor() {
        scope.launch {
            runCatching { config.signaling.onTakeFloor() }
        }
    }

    private fun onReleaseFloor() {
        scope.launch {
            runCatching { config.signaling.onReleaseFloor() }
        }
    }

    private fun createRTCConfiguration(): PeerConnection.RTCConfiguration {
        val iceServers = config.iceServers.map {
            PeerConnection.IceServer.builder(it.urls.toList())
                .setUsername(it.username)
                .setPassword(it.password)
                .createIceServer()
        }
        val c = PeerConnection.RTCConfiguration(iceServers)
        c.enableDscp = config.dscp
        c.sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN
        c.continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY
        return c
    }

    private val RtpReceiver.videoTrack
        get() = track() as? org.webrtc.VideoTrack
}
