/*
 * Copyright 2022-2024 Pexip AS
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
import com.pexip.sdk.media.CandidateSignalingEvent
import com.pexip.sdk.media.DataSender
import com.pexip.sdk.media.DegradationPreference
import com.pexip.sdk.media.LocalAudioTrack
import com.pexip.sdk.media.LocalMediaTrack
import com.pexip.sdk.media.LocalVideoTrack
import com.pexip.sdk.media.MediaConnection
import com.pexip.sdk.media.MediaConnectionConfig
import com.pexip.sdk.media.MediaConnectionSignaling
import com.pexip.sdk.media.OfferSignalingEvent
import com.pexip.sdk.media.RestartSignalingEvent
import com.pexip.sdk.media.SecureCheckCode
import com.pexip.sdk.media.VideoTrack
import com.pexip.sdk.media.coroutines.getCapturing
import com.pexip.sdk.media.webrtc.WebRtcMediaConnectionFactory
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.webrtc.IceCandidate
import org.webrtc.MediaStreamTrack.MediaType
import org.webrtc.NetworkMonitor
import org.webrtc.PeerConnection
import org.webrtc.RtpParameters
import org.webrtc.RtpTransceiver.RtpTransceiverDirection
import org.webrtc.SessionDescription
import java.util.concurrent.CopyOnWriteArraySet
import kotlin.coroutines.CoroutineContext

@OptIn(ExperimentalCoroutinesApi::class)
internal class WebRtcMediaConnection(
    factory: WebRtcMediaConnectionFactory,
    context: CoroutineContext,
    private val config: MediaConnectionConfig,
    private val signalingDispatcher: CoroutineDispatcher,
) : MediaConnection {

    private val handler = CoroutineExceptionHandler { _, t ->
        when (t) {
            is CancellationException -> throw t
            else -> {} // Do nothing
        }
    }

    private val scope = CoroutineScope(context + handler)

    private val mutex = Mutex()
    private var polite = false
    private var makingOffer = false
    private var ignoreOffer = false
    private var signalingState = PeerConnection.SignalingState.STABLE

    private val wrapper = factory.createPeerConnection(config)

    private val maxBitrate = MutableStateFlow(0.bps)
    private val mainDegradationPreference = MutableStateFlow(DegradationPreference.BALANCED)
    private val presentationDegradationPreference = MutableStateFlow(DegradationPreference.BALANCED)

    private val mainRemoteVideoTrackPreferredAspectRatio = MutableStateFlow(Float.NaN)

    private val mainLocalAudioTrack = MutableSharedFlow<LocalAudioTrack?>()
    private val mainLocalVideoTrack = MutableSharedFlow<LocalVideoTrack?>()
    private val presentationLocalVideoTrack = MutableSharedFlow<LocalVideoTrack?>()

    private val mainRemoteVideoTrackListeners =
        CopyOnWriteArraySet<MediaConnection.RemoteVideoTrackListener>()
    private val presentationRemoteVideoTrackListeners =
        CopyOnWriteArraySet<MediaConnection.RemoteVideoTrackListener>()

    // Using MutableStateFlow instead of stateIn to be able to set the value to null after the
    // CoroutineScope cancellation
    private val _mainRemoteVideoTrack = MutableStateFlow<VideoTrack?>(null)
    private val _presentationRemoteVideoTrack = MutableStateFlow<VideoTrack?>(null)

    override val secureCheckCode: StateFlow<SecureCheckCode?> =
        wrapper.secureCheckCode.stateIn(scope, SharingStarted.Eagerly, null)

    override val mainRemoteVideoTrack: VideoTrack?
        get() = _mainRemoteVideoTrack.value

    override val presentationRemoteVideoTrack: VideoTrack?
        get() = _presentationRemoteVideoTrack.value

    init {
        with(scope) {
            launchDataSender()
            if (config.signaling.directMedia) {
                scope.launch {
                    val init = RtpTransceiverInit(RtpTransceiverDirection.INACTIVE)
                    wrapper.withRtpTransceiver(MainAudio, init) { }
                    wrapper.withRtpTransceiver(MainVideo, init) { }
                    wrapper.withRtpTransceiver(PresentationVideo, init) { }
                }
            }
            launchConnectionType()
            launchMaxBitrate()
            launchWrapperEvent()
            launchSignalingEvent()
            launchPreferredAspectRatio()
            launchDegradationPreference(MainVideo, mainDegradationPreference)
            launchDegradationPreference(PresentationVideo, presentationDegradationPreference)
            launchLocalMediaTrackCapturing(mainLocalAudioTrack) {
                if (it) onAudioUnmuted() else onAudioMuted()
            }
            launchLocalMediaTrackCapturing(mainLocalVideoTrack) {
                if (it) onVideoUnmuted() else onVideoMuted()
            }
            launchLocalMediaTrackCapturing(presentationLocalVideoTrack) {
                if (it) onTakeFloor() else onReleaseFloor()
            }
            launchRemoteVideoTrackListeners(_mainRemoteVideoTrack, mainRemoteVideoTrackListeners)
            launchRemoteVideoTrackListeners(
                flow = _presentationRemoteVideoTrack,
                listeners = presentationRemoteVideoTrackListeners,
            )
            launchRemoteVideoTrack(MainVideo, _mainRemoteVideoTrack)
            launchRemoteVideoTrack(PresentationVideo, _presentationRemoteVideoTrack)
            launchDispose()
        }
    }

    override fun setMainAudioTrack(localAudioTrack: LocalAudioTrack?) {
        scope.launchLocalMediaTrack(
            key = MainAudio,
            track = when (localAudioTrack) {
                is WebRtcLocalAudioTrack -> localAudioTrack
                null -> null
                else -> throw IllegalArgumentException("localAudioTrack must be null or an instance of WebRtcLocalAudioTrack.")
            },
            emit = mainLocalAudioTrack::emit,
        )
    }

    override fun setMainVideoTrack(localVideoTrack: LocalVideoTrack?) {
        scope.launchLocalMediaTrack(
            key = MainVideo,
            track = when (localVideoTrack) {
                is WebRtcLocalVideoTrack -> localVideoTrack
                null -> null
                else -> throw IllegalArgumentException("localVideoTrack must be null or an instance of WebRtcLocalVideoTrack.")
            },
            preference = mainDegradationPreference::value,
            emit = mainLocalVideoTrack::emit,
        )
    }

    override fun setPresentationVideoTrack(localVideoTrack: LocalVideoTrack?) {
        scope.launchLocalMediaTrack(
            key = PresentationVideo,
            track = when (localVideoTrack) {
                is WebRtcLocalVideoTrack -> localVideoTrack
                null -> null
                else -> throw IllegalArgumentException("localVideoTrack must be null or an instance of WebRtcLocalVideoTrack.")
            },
            preference = presentationDegradationPreference::value,
            emit = presentationLocalVideoTrack::emit,
        )
    }

    override fun setMainRemoteAudioTrackEnabled(enabled: Boolean) {
        scope.launchRemoteVideoTrackEnabled(MainAudio, enabled)
    }

    override fun setMainRemoteVideoTrackEnabled(enabled: Boolean) {
        scope.launchRemoteVideoTrackEnabled(MainVideo, enabled)
    }

    override fun setPresentationRemoteVideoTrackEnabled(enabled: Boolean) {
        if (!config.presentationInMain) {
            scope.launchRemoteVideoTrackEnabled(PresentationVideo, enabled)
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

    override fun setMainRemoteVideoTrackPreferredAspectRatio(aspectRatio: Float) {
        mainRemoteVideoTrackPreferredAspectRatio.value = aspectRatio
    }

    override fun dtmf(digits: String) {
        scope.launch { runCatching { config.signaling.onDtmf(digits) } }
    }

    override fun start() {
        scope.launch { wrapper.start() }
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

    private fun CoroutineScope.launchDataSender() = launch {
        val sender = DataSender { (data, binary) -> wrapper.send(data, binary) }
        try {
            config.signaling.attach(sender)
            awaitCancellation()
        } finally {
            withContext(NonCancellable) {
                config.signaling.detach(sender)
            }
        }
    }

    private fun CoroutineScope.launchMaxBitrate() = maxBitrate.drop(1)
        .onEach { wrapper.restartIce() }
        .launchIn(this)

    private fun CoroutineScope.launchConnectionType() = NetworkMonitor.getInstance()
        .connectionType()
        .onEach { wrapper.restartIce() }
        .launchIn(this)

    private fun CoroutineScope.launchWrapperEvent() = launch {
        wrapper.event.collect {
            when (it) {
                is Event.OnSignalingChange -> mutex.withLock {
                    signalingState = it.state
                }
                is Event.OnRenegotiationNeeded -> {
                    val bitrate = maxBitrate.value
                    val answer = try {
                        mutex.withLock { makingOffer = true }
                        val localDescription = wrapper.setLocalDescription { mids ->
                            mangle(
                                bitrate = bitrate,
                                mainAudioMid = mids[MainAudio],
                                mainVideoMid = mids[MainVideo],
                                presentationVideoMid = mids[PresentationVideo],
                            )
                        }
                        config.signaling.onOffer(
                            callType = "WEBRTC",
                            description = localDescription.description,
                            presentationInMain = config.presentationInMain,
                            fecc = config.farEndCameraControl,
                        )
                    } finally {
                        mutex.withLock { makingOffer = false }
                    }
                    if (answer == null) {
                        mutex.withLock { polite = true }
                        return@collect
                    }
                    val sdp = SessionDescription(SessionDescription.Type.ANSWER, answer)
                    try {
                        wrapper.setRemoteDescription(sdp.mangle(bitrate))
                    } catch (e: RuntimeException) {
                        return@collect
                    }
                    runCatching { config.signaling.onAck() }
                }
                is Event.OnIceCandidate -> {
                    val sdp = it.candidate.sdp ?: return@collect
                    val mid = it.candidate.sdpMid ?: return@collect
                    val credentials = wrapper.getIceCredentials(mid) ?: return@collect
                    launch {
                        runCatching {
                            config.signaling.onCandidate(
                                candidate = sdp,
                                mid = mid,
                                ufrag = credentials.ufrag,
                                pwd = credentials.pwd,
                            )
                        }
                    }
                }
                is Event.OnIceConnectionChange -> {
                    if (it.state != PeerConnection.IceConnectionState.FAILED) return@collect
                    wrapper.restartIce()
                }
                is Event.OnData -> {
                    config.signaling.onData(it.data)
                }
                else -> Unit
            }
        }
    }

    private fun CoroutineScope.launchSignalingEvent() = launch {
        config.signaling.event.collect { event ->
            when (event) {
                is OfferSignalingEvent -> {
                    val ignoreOffer = mutex.withLock {
                        val stable = signalingState == PeerConnection.SignalingState.STABLE
                        val offerCollision = makingOffer || !stable
                        (!polite && offerCollision).also { ignoreOffer = it }
                    }
                    if (ignoreOffer) {
                        runCatching { config.signaling.onOfferIgnored() }
                        return@collect
                    }
                    val description =
                        SessionDescription(SessionDescription.Type.OFFER, event.description)
                    wrapper.setRemoteDescription(description.mangle(maxBitrate.value))
                    val localDescription = wrapper.setLocalDescription { mids ->
                        mangle(
                            bitrate = maxBitrate.value,
                            mainAudioMid = mids[MainAudio],
                            mainVideoMid = mids[MainVideo],
                            presentationVideoMid = mids[PresentationVideo],
                        )
                    }
                    runCatching { config.signaling.onAnswer(localDescription.description) }
                }
                is CandidateSignalingEvent -> try {
                    if (event.candidate.isBlank()) return@collect
                    val candidate = IceCandidate(event.mid, -1, event.candidate)
                    wrapper.addIceCandidate(candidate)
                } catch (e: CancellationException) {
                    throw e
                } catch (t: Throwable) {
                    if (mutex.withLock { !ignoreOffer }) {
                        throw t
                    }
                }
                is RestartSignalingEvent -> {
                    wrapper.restart()
                }
            }
        }
    }

    private fun CoroutineScope.launchDegradationPreference(
        key: RtpTransceiverKey,
        flow: Flow<DegradationPreference>,
    ) = flow.drop(1)
        .onEach { p -> wrapper.withRtpTransceiver(key) { it?.setDegradationPreference(p) } }
        .launchIn(this)

    private fun <T : LocalMediaTrack> CoroutineScope.launchLocalMediaTrack(
        key: RtpTransceiverKey,
        track: T?,
        preference: (() -> DegradationPreference)? = null,
        emit: suspend (T?) -> Unit,
    ) = launch {
        val init = when (track) {
            null -> null
            else -> RtpTransceiverInit(RtpTransceiverDirection.SEND_ONLY)
        }
        wrapper.withRtpTransceiver(key, init) { rtpTransceiver ->
            rtpTransceiver ?: return@withRtpTransceiver
            rtpTransceiver.setTrack(track)
            rtpTransceiver.maybeSetNewDirection(track)
            preference?.let { rtpTransceiver.setDegradationPreference(it()) }
        }
        emit(track)
    }

    private fun CoroutineScope.launchPreferredAspectRatio() =
        mainRemoteVideoTrackPreferredAspectRatio
            .filterNot(Float::isNaN)
            .onEach(config.signaling::onPreferredAspectRatio)
            .launchIn(this)

    private fun CoroutineScope.launchLocalMediaTrackCapturing(
        flow: Flow<LocalMediaTrack?>,
        onCapturing: suspend MediaConnectionSignaling.(Boolean) -> Unit,
    ) = flow
        .flatMapLatest {
            when (it) {
                null -> flowOf(false)
                else -> it.getCapturing().onStart { emit(it.capturing) }
            }
        }
        .onEach { runCatching { config.signaling.onCapturing(it) } }
        .launchIn(this)

    private fun CoroutineScope.launchRemoteVideoTrackListeners(
        flow: Flow<VideoTrack?>,
        listeners: Collection<MediaConnection.RemoteVideoTrackListener>,
    ) = flow.drop(1)
        .onEach { track -> listeners.forEach { it.safeOnRemoteVideoTrack(track) } }
        .flowOn(signalingDispatcher)
        .launchIn(this)

    private fun CoroutineScope.launchRemoteVideoTrack(
        key: RtpTransceiverKey,
        flow: MutableStateFlow<VideoTrack?>,
    ) = wrapper.getRemoteVideoTrack(key)
        .map { track -> track?.let { WebRtcVideoTrack(it, this) } }
        .onEach { flow.value = it }
        .onCompletion { flow.value = null }
        .launchIn(this)

    private fun CoroutineScope.launchRemoteVideoTrackEnabled(
        key: RtpTransceiverKey,
        enabled: Boolean,
    ) = launch {
        val init = when (enabled) {
            true -> RtpTransceiverInit(RtpTransceiverDirection.RECV_ONLY)
            else -> null
        }
        wrapper.withRtpTransceiver(key, init) { it?.maybeSetNewDirection(enabled) }
    }

    private fun CoroutineScope.launchDispose() = launch {
        try {
            awaitCancellation()
        } finally {
            withContext(NonCancellable) {
                mainRemoteVideoTrackListeners.clear()
                presentationRemoteVideoTrackListeners.clear()
                wrapper.dispose()
            }
        }
    }

    @Suppress("FunctionName")
    private fun RtpTransceiverInit(direction: RtpTransceiverDirection) = { key: RtpTransceiverKey ->
        RtpTransceiverInit(
            direction = direction,
            streamIds = key.streamIds,
            sendEncodings = key.sendEncodings,
        )
    }

    private data object MainAudio : RtpTransceiverKey {

        override val mediaType: MediaType = MediaType.MEDIA_TYPE_AUDIO
        override val streamIds: List<String> = listOf("main")
    }

    private data object MainVideo : RtpTransceiverKey {

        override val mediaType: MediaType = MediaType.MEDIA_TYPE_VIDEO
        override val streamIds: List<String> = listOf("main")
        override val sendEncodings: List<RtpParameters.Encoding> =
            listOf(Encoding { maxFramerate = MAX_FRAMERATE })
    }

    private data object PresentationVideo : RtpTransceiverKey {

        override val mediaType: MediaType = MediaType.MEDIA_TYPE_VIDEO
        override val streamIds: List<String> = listOf("slides")
        override val sendEncodings: List<RtpParameters.Encoding> =
            listOf(Encoding { maxFramerate = MAX_FRAMERATE })
    }
}
