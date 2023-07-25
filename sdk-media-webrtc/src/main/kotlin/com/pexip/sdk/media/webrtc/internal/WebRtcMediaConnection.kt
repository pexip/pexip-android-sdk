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
import com.pexip.sdk.media.VideoTrack
import com.pexip.sdk.media.coroutines.getCapturing
import com.pexip.sdk.media.webrtc.WebRtcMediaConnectionFactory
import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
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

internal class WebRtcMediaConnection(
    factory: WebRtcMediaConnectionFactory,
    private val config: MediaConnectionConfig,
    private val job: CompletableJob,
) : MediaConnection, SimplePeerConnectionObserver {

    private val started = AtomicBoolean()
    private val shouldAck = AtomicBoolean(true)
    private val shouldRenegotiate = AtomicBoolean()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val dispatcher = Dispatchers.IO.limitedParallelism(1)
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)
    private val iceCredentials = mutableMapOf<String, IceCredentials>()
    private val connection = factory.createPeerConnection(createRTCConfiguration(), this)

    private val maxBitrate = MutableStateFlow(0.bps)
    private val mainDegradationPreference = MutableStateFlow(DegradationPreference.BALANCED)
    private val presentationDegradationPreference = MutableStateFlow(DegradationPreference.BALANCED)

    private val mainLocalAudioTrack = MutableSharedFlow<LocalAudioTrack?>()
    private val mainLocalVideoTrack = MutableSharedFlow<LocalVideoTrack?>()
    private val presentationLocalVideoTrack = MutableSharedFlow<LocalVideoTrack?>()

    private var mainAudioTransceiver: RtpTransceiver? = null
    private var mainVideoTransceiver: RtpTransceiver? = null
    private val presentationVideoTransceiver: RtpTransceiver = connection.addTransceiver(
        MediaType.MEDIA_TYPE_VIDEO,
        RtpTransceiverInit(
            direction = RtpTransceiverDirection.INACTIVE,
            sendEncodings = listOf(Encoding { maxFramerate = MAX_FRAMERATE }),
        ),
    ).apply { setDegradationPreference(presentationDegradationPreference.value) }
    private val mainRemoteVideoTrackListeners =
        CopyOnWriteArraySet<MediaConnection.RemoteVideoTrackListener>()
    private val presentationRemoteVideoTrackListeners =
        CopyOnWriteArraySet<MediaConnection.RemoteVideoTrackListener>()
    private val _mainRemoteVideoTrack = MutableStateFlow<VideoTrack?>(null)
    private val _presentationRemoteVideoTrack = MutableStateFlow<VideoTrack?>(null)

    override val mainRemoteVideoTrack: VideoTrack?
        get() = _mainRemoteVideoTrack.value

    override val presentationRemoteVideoTrack: VideoTrack?
        get() = _presentationRemoteVideoTrack.value

    init {
        with(scope) {
            launchMaxBitrate()
            launchMainDegradationPreference()
            launchPresentationDegradationPreference()
            launchMainRemoteVideoTrackListeners()
            launchPresentationRemoteVideoTrackListeners()
            launchMainLocalAudioTrack()
            launchMainLocalVideoTrack()
            launchPresentationLocalVideoTrack()
            launchDispose()
        }
    }

    override fun setMainAudioTrack(localAudioTrack: LocalAudioTrack?) {
        val lat = when (localAudioTrack) {
            is WebRtcLocalAudioTrack -> localAudioTrack
            null -> null
            else -> throw IllegalArgumentException("localAudioTrack must be null or an instance of WebRtcLocalAudioTrack.")
        }
        scope.launch { mainLocalAudioTrack.emit(lat) }
    }

    override fun setMainVideoTrack(localVideoTrack: LocalVideoTrack?) {
        val lvt = when (localVideoTrack) {
            is WebRtcLocalVideoTrack -> localVideoTrack
            null -> null
            else -> throw IllegalArgumentException("localVideoTrack must be null or an instance of WebRtcLocalVideoTrack.")
        }
        scope.launch { mainLocalVideoTrack.emit(lvt) }
    }

    override fun setPresentationVideoTrack(localVideoTrack: LocalVideoTrack?) {
        val lvt = when (localVideoTrack) {
            is WebRtcLocalVideoTrack -> localVideoTrack
            null -> null
            else -> throw IllegalArgumentException("localVideoTrack must be null or an instance of WebRtcLocalVideoTrack.")
        }
        scope.launch { presentationLocalVideoTrack.emit(lvt) }
    }

    override fun setMainRemoteAudioTrackEnabled(enabled: Boolean) {
        scope.launch {
            mainAudioTransceiver = mainAudioTransceiver ?: connection.maybeAddTransceiver(
                mediaType = MediaType.MEDIA_TYPE_AUDIO,
                receive = enabled,
            )
            mainAudioTransceiver?.maybeSetNewDirection(enabled)
        }
    }

    override fun setMainRemoteVideoTrackEnabled(enabled: Boolean) {
        scope.launch {
            mainVideoTransceiver = mainVideoTransceiver ?: connection.maybeAddTransceiver(
                mediaType = MediaType.MEDIA_TYPE_VIDEO,
                receive = enabled,
            )
            mainVideoTransceiver?.maybeSetNewDirection(enabled)
        }
    }

    override fun setPresentationRemoteVideoTrackEnabled(enabled: Boolean) {
        if (!config.presentationInMain) {
            scope.launch {
                presentationVideoTransceiver.maybeSetNewDirection(enabled)
            }
        }
    }

    override fun setMaxBitrate(bitrate: Bitrate) {
        maxBitrate.value = bitrate
    }

    override fun setMainDegradationPreference(preference: DegradationPreference) {
        mainDegradationPreference.value = preference
    }

    override fun setPresentationDegradationPreference(preference: DegradationPreference) {
        presentationDegradationPreference.value = preference
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
            scope.launch { setLocalDescription() }
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
        scope.launch { connection.restartIce() }
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
        scope.launch { setLocalDescription() }
    }

    override fun onAddTrack(receiver: RtpReceiver, streams: Array<out MediaStream>) {
        val id = receiver.id()
        val videoTrack = receiver.videoTrack?.let(::WebRtcVideoTrack)
        scope.launch {
            when (id) {
                mainVideoTransceiver?.receiver?.id() -> {
                    _mainRemoteVideoTrack.emit(videoTrack)
                }
                presentationVideoTransceiver.receiver.id() -> {
                    _presentationRemoteVideoTrack.emit(videoTrack)
                }
            }
        }
    }

    override fun onRemoveTrack(receiver: RtpReceiver) {
        val id = receiver.id()
        scope.launch {
            when (id) {
                mainVideoTransceiver?.receiver?.id() -> {
                    _mainRemoteVideoTrack.emit(null)
                }
                presentationVideoTransceiver.receiver.id() -> {
                    _presentationRemoteVideoTrack.emit(null)
                }
            }
        }
    }

    private suspend fun setLocalDescription() {
        connection.setLocalDescription()
        val bitrate = maxBitrate.value
        val result = connection.localDescription.mangle(
            bitrate = bitrate,
            mainAudioMid = mainAudioTransceiver?.mid,
            mainVideoMid = mainVideoTransceiver?.mid,
            presentationVideoMid = presentationVideoTransceiver.mid,
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
        }
    }

    private fun CoroutineScope.launchMaxBitrate() = maxBitrate.drop(1)
        .onEach { connection.restartIce() }
        .launchIn(this)

    private fun CoroutineScope.launchMainDegradationPreference() = mainDegradationPreference.drop(1)
        .onEach { mainVideoTransceiver?.setDegradationPreference(it) }
        .launchIn(this)

    private fun CoroutineScope.launchPresentationDegradationPreference() =
        presentationDegradationPreference.drop(1)
            .onEach { presentationVideoTransceiver.setDegradationPreference(it) }
            .launchIn(this)

    private fun CoroutineScope.launchMainLocalAudioTrack() = launch {
        mainLocalAudioTrack.distinctUntilChanged().collectLatest { track ->
            val transceiver = mainAudioTransceiver ?: connection.maybeAddTransceiver(track)
            transceiver?.maybeSetNewDirection(track)
            transceiver?.setTrack(track)
            mainAudioTransceiver = transceiver
            track.getCapturingOrFalse().collectLatest {
                runCatching {
                    with(config.signaling) { if (it) onAudioUnmuted() else onAudioMuted() }
                }
            }
        }
    }

    private fun CoroutineScope.launchMainLocalVideoTrack() = launch {
        mainLocalVideoTrack.distinctUntilChanged().collectLatest { track ->
            val transceiver = mainVideoTransceiver ?: connection.maybeAddTransceiver(track)?.apply {
                setDegradationPreference(mainDegradationPreference.value)
            }
            transceiver?.maybeSetNewDirection(track)
            transceiver?.setTrack(track)
            mainVideoTransceiver = transceiver
            track.getCapturingOrFalse().collectLatest {
                runCatching {
                    with(config.signaling) { if (it) onVideoUnmuted() else onVideoMuted() }
                }
            }
        }
    }

    private fun CoroutineScope.launchPresentationLocalVideoTrack() = launch {
        presentationLocalVideoTrack.distinctUntilChanged().collectLatest { track ->
            presentationVideoTransceiver.maybeSetNewDirection(track)
            presentationVideoTransceiver.setTrack(track)
            track.getCapturingOrFalse().collectLatest {
                runCatching {
                    with(config.signaling) { if (it) onTakeFloor() else onReleaseFloor() }
                }
            }
        }
    }

    private fun CoroutineScope.launchMainRemoteVideoTrackListeners() = _mainRemoteVideoTrack.drop(1)
        .onEach { mainRemoteVideoTrackListeners.notify(it) }
        .flowOn(Dispatchers.Main)
        .launchIn(this)

    private fun CoroutineScope.launchPresentationRemoteVideoTrackListeners() =
        _presentationRemoteVideoTrack.drop(1)
            .onEach { presentationRemoteVideoTrackListeners.notify(it) }
            .flowOn(Dispatchers.Main)
            .launchIn(this)

    private fun CoroutineScope.launchDispose() = launch {
        try {
            awaitCancellation()
        } finally {
            withContext(NonCancellable) {
                mainAudioTransceiver?.sender?.setTrack(null, false)
                mainVideoTransceiver?.sender?.setTrack(null, false)
                presentationVideoTransceiver.sender.setTrack(null, false)
                _mainRemoteVideoTrack.emit(null)
                _presentationRemoteVideoTrack.emit(null)
                mainRemoteVideoTrackListeners.clear()
                presentationRemoteVideoTrackListeners.clear()
                connection.dispose()
                job.complete()
            }
        }
    }

    private fun LocalMediaTrack?.getCapturingOrFalse() = when (this) {
        null -> flowOf(false)
        else -> getCapturing()
            .onStart { emit(capturing) }
            .distinctUntilChanged()
    }

    private fun Collection<MediaConnection.RemoteVideoTrackListener>.notify(videoTrack: VideoTrack?) =
        forEach { it.safeOnRemoteVideoTrack(videoTrack) }

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
