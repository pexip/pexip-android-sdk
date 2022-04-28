package com.pexip.sdk.media.webrtc

import android.content.Context
import com.pexip.sdk.media.CapturingListener
import com.pexip.sdk.media.IceServer
import com.pexip.sdk.media.LocalAudioTrack
import com.pexip.sdk.media.MediaConnection
import com.pexip.sdk.media.MediaConnectionConfig
import com.pexip.sdk.media.MediaConnectionSignaling
import com.pexip.sdk.media.QualityProfile
import com.pexip.sdk.media.webrtc.internal.SimplePeerConnectionObserver
import com.pexip.sdk.media.webrtc.internal.SimpleSdpObserver
import com.pexip.sdk.media.webrtc.internal.WebRtcLocalAudioTrack
import com.pexip.sdk.media.webrtc.internal.mangle
import org.webrtc.CameraVideoCapturer
import org.webrtc.EglBase
import org.webrtc.IceCandidate
import org.webrtc.MediaConstraints
import org.webrtc.MediaStreamTrack
import org.webrtc.PeerConnection
import org.webrtc.RtpTransceiver
import org.webrtc.RtpTransceiver.RtpTransceiverDirection
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription
import org.webrtc.SurfaceTextureHelper
import org.webrtc.VideoSource
import org.webrtc.VideoTrack
import java.util.concurrent.CopyOnWriteArraySet
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

public class WebRtcMediaConnection internal constructor(
    private val factory: WebRtcMediaConnectionFactory,
    private val config: MediaConnectionConfig,
    private val workerExecutor: ExecutorService,
    private val signalingExecutor: ExecutorService,
    private val shouldDisposeFactory: Boolean = false,
) : MediaConnection {

    private val started = AtomicBoolean()
    private val observer: PeerConnection.Observer = object : SimplePeerConnectionObserver {

        private val shouldRenegotiate = AtomicBoolean()

        override fun onIceCandidate(candidate: IceCandidate) {
            onCandidate(candidate.sdp, candidate.sdpMid)
        }

        override fun onRenegotiationNeeded() {
            // Skip the first call to onRenegotiationNeeded() since it's called right after
            // PeerConnection creation and we're still not ready to use createOffer()
            if (shouldRenegotiate.compareAndSet(false, true)) return
            createOffer()
        }
    }
    private val connection = createPeerConnection()
    private var mainVideoCapturer: CameraVideoCapturer? = null
    private var mainVideoCapturing: Boolean = false
    private var mainVideoSource: VideoSource? = null
    private var mainVideoTrack: VideoTrack? = null
    private var mainVideoSurfaceTextureHelper: SurfaceTextureHelper? = null
    private var mainAudioTransceiver: RtpTransceiver? = null
    private var mainVideoTransceiver: RtpTransceiver? = null
    private val presentationVideoTransceiver: RtpTransceiver? = when (config.presentationInMain) {
        true -> null
        else -> connection.addTransceiver(
            MediaStreamTrack.MediaType.MEDIA_TYPE_VIDEO,
            RtpTransceiver.RtpTransceiverInit(RtpTransceiverDirection.INACTIVE)
        )
    }
    private val localSdpObserver = object : SimpleSdpObserver {

        override fun onCreateSuccess(description: SessionDescription) {
            setLocalDescription(this)
        }

        override fun onSetSuccess() {
            val mangledDescription = connection.localDescription.mangle(
                mainAudioMid = mainAudioTransceiver?.mid,
                mainVideoMid = mainVideoTransceiver?.mid,
                presentationVideoMid = presentationVideoTransceiver?.mid
            )
            onSetLocalDescriptionSuccess(mangledDescription)
        }
    }
    private val remoteSdpObserver = object : SimpleSdpObserver {}
    private val mainVideoCapturingListeners = CopyOnWriteArraySet<CapturingListener>()
    private val mainLocalVideoTrackListeners = CopyOnWriteArraySet<VideoTrackListener>()
    private val mainRemoteVideoTrackListeners = CopyOnWriteArraySet<VideoTrackListener>()
    private val presentationLocalVideoTrackListeners = CopyOnWriteArraySet<VideoTrackListener>()
    private val presentationRemoteVideoTrackListeners = CopyOnWriteArraySet<VideoTrackListener>()

    @Deprecated("Use WebRtcMediaConnectionFactory.eglBaseContext instead.")
    public val eglBaseContext: EglBase.Context
        get() = factory.eglBaseContext

    override fun sendMainAudio(localAudioTrack: LocalAudioTrack) {
        require(localAudioTrack is WebRtcLocalAudioTrack) { "localAudioTrack must be an instance of WebRtcLocalAudioTrack." }
        sendMainAudioInternal(localAudioTrack)
    }

    override fun sendMainVideo() {
        sendMainVideoInternal()
    }

    override fun sendMainVideo(deviceName: String) {
        require(deviceName in factory.cameraEnumerator.deviceNames) { "deviceName is not available." }
        sendMainVideoInternal(deviceName)
    }

    override fun startMainCapture() {
        workerExecutor.maybeExecute {
            val capturer = mainVideoCapturer ?: return@maybeExecute
            capturer.startCapture(
                config.mainQualityProfile.width,
                config.mainQualityProfile.height,
                config.mainQualityProfile.fps
            )
            onVideoUnmuted()
            mainVideoCapturing = true
            mainVideoCapturingListeners.forEach { it.onCapturing(true) }
        }
    }

    override fun stopMainCapture() {
        workerExecutor.maybeExecute {
            val capturer = mainVideoCapturer ?: return@maybeExecute
            capturer.stopCapture()
            onVideoMuted()
            mainVideoCapturing = false
            mainVideoCapturingListeners.forEach { it.onCapturing(false) }
        }
    }

    override fun startPresentationReceive() {
        workerExecutor.maybeExecute {
            val t = presentationVideoTransceiver
                ?.takeUnless { it.currentDirection == RtpTransceiverDirection.RECV_ONLY }
                ?: return@maybeExecute
            t.direction = RtpTransceiverDirection.RECV_ONLY
            val videoTrack = t.receiver.track() as? VideoTrack
            presentationRemoteVideoTrackListeners.forEach { it.onVideoTrack(videoTrack) }
        }
    }

    override fun stopPresentationReceive() {
        workerExecutor.maybeExecute {
            val t = presentationVideoTransceiver
                ?.takeUnless { it.currentDirection == RtpTransceiverDirection.INACTIVE }
                ?: return@maybeExecute
            t.direction = RtpTransceiverDirection.INACTIVE
            presentationRemoteVideoTrackListeners.forEach { it.onVideoTrack(null) }
        }
    }

    override fun start() {
        if (started.compareAndSet(false, true)) {
            createOffer()
        }
    }

    @Synchronized
    override fun dispose() {
        workerExecutor.maybeExecute {
            mainLocalVideoTrackListeners.clear()
            mainRemoteVideoTrackListeners.clear()
            presentationLocalVideoTrackListeners.clear()
            presentationRemoteVideoTrackListeners.clear()
            connection.dispose()
            mainVideoTrack?.dispose()
            mainVideoCapturer?.stopCapture()
            mainVideoSurfaceTextureHelper?.dispose()
            mainVideoSource?.dispose()
            mainVideoCapturer?.dispose()
            if (shouldDisposeFactory) {
                factory.dispose()
            }
        }
        workerExecutor.shutdown()
        signalingExecutor.shutdown()
    }

    public fun registerMainLocalVideoTrackListener(listener: VideoTrackListener) {
        workerExecutor.maybeExecute {
            listener.onVideoTrack(mainVideoTransceiver?.sender?.track() as? VideoTrack)
        }
        mainLocalVideoTrackListeners += listener
    }

    public fun unregisterMainLocalVideoTrackListener(listener: VideoTrackListener) {
        mainLocalVideoTrackListeners -= listener
    }

    public fun registerMainRemoteVideoTrackListener(listener: VideoTrackListener) {
        workerExecutor.maybeExecute {
            listener.onVideoTrack(mainVideoTransceiver?.receiver?.track() as? VideoTrack)
        }
        mainRemoteVideoTrackListeners += listener
    }

    public fun unregisterMainRemoteVideoTrackListener(listener: VideoTrackListener) {
        mainRemoteVideoTrackListeners -= listener
    }

    public fun registerPresentationLocalVideoTrackListener(listener: VideoTrackListener) {
        workerExecutor.maybeExecute {
            listener.onVideoTrack(presentationVideoTransceiver?.sender?.track() as? VideoTrack)
        }
        presentationLocalVideoTrackListeners += listener
    }

    public fun unregisterPresentationLocalVideoTrackListener(listener: VideoTrackListener) {
        presentationLocalVideoTrackListeners -= listener
    }

    public fun registerPresentationRemoteVideoTrackListener(listener: VideoTrackListener) {
        workerExecutor.maybeExecute {
            val t = presentationVideoTransceiver
                ?.takeIf { it.currentDirection == RtpTransceiverDirection.SEND_RECV }
                ?: return@maybeExecute
            listener.onVideoTrack(t.receiver.track() as? VideoTrack)
        }
        presentationRemoteVideoTrackListeners += listener
    }

    public fun unregisterPresentationRemoteVideoTrackListener(listener: VideoTrackListener) {
        presentationRemoteVideoTrackListeners -= listener
    }

    override fun registerMainCapturingListener(listener: CapturingListener) {
        workerExecutor.maybeExecute {
            listener.onCapturing(mainVideoCapturing)
        }
        mainVideoCapturingListeners += listener
    }

    override fun unregisterMainCapturingListener(listener: CapturingListener) {
        mainVideoCapturingListeners -= listener
    }

    private fun sendMainAudioInternal(localAudioTrack: WebRtcLocalAudioTrack) {
        workerExecutor.maybeExecute {
            if (mainAudioTransceiver == null) {
                val transceiver = connection.addTransceiver(
                    MediaStreamTrack.MediaType.MEDIA_TYPE_AUDIO,
                    RtpTransceiver.RtpTransceiverInit(RtpTransceiverDirection.SEND_RECV)
                )
                transceiver.sender.setTrack(localAudioTrack.audioTrack, false)
                mainAudioTransceiver = transceiver
            }
        }
    }

    @Suppress("NAME_SHADOWING")
    private fun sendMainVideoInternal(deviceName: String? = null) {
        workerExecutor.maybeExecute {
            if (mainVideoTransceiver == null) {
                val deviceNames = factory.cameraEnumerator.deviceNames
                val deviceName = deviceName
                    ?: deviceNames.firstOrNull(factory.cameraEnumerator::isFrontFacing)
                    ?: deviceNames.firstOrNull(factory.cameraEnumerator::isBackFacing)
                    ?: deviceNames.first()
                mainVideoCapturer = factory.cameraEnumerator.createCapturer(deviceName, null)
                mainVideoSource =
                    factory.factory.createVideoSource(mainVideoCapturer!!.isScreencast)
                mainVideoTrack = factory.factory.createVideoTrack("ARDAMSv0", mainVideoSource)
                val transceiver = connection.addTransceiver(
                    MediaStreamTrack.MediaType.MEDIA_TYPE_VIDEO,
                    RtpTransceiver.RtpTransceiverInit(RtpTransceiverDirection.SEND_RECV)
                )
                transceiver.sender.setTrack(mainVideoTrack, false)
                mainVideoTransceiver = transceiver
                mainLocalVideoTrackListeners.forEach {
                    it.onVideoTrack(transceiver.sender.track() as? VideoTrack)
                }
                mainRemoteVideoTrackListeners.forEach {
                    it.onVideoTrack(transceiver.receiver.track() as? VideoTrack)
                }
                mainVideoSurfaceTextureHelper =
                    SurfaceTextureHelper.create("main", factory.eglBaseContext)
                mainVideoCapturer?.initialize(
                    mainVideoSurfaceTextureHelper,
                    factory.applicationContext,
                    mainVideoSource?.capturerObserver
                )
            }
        }
    }

    private fun createOffer() {
        workerExecutor.maybeExecute {
            connection.createOffer(localSdpObserver, MediaConstraints())
        }
    }

    private fun setLocalDescription(observer: SdpObserver) {
        workerExecutor.maybeExecute {
            connection.setLocalDescription(observer)
        }
    }

    private fun setRemoteDescription(sdp: SessionDescription) {
        workerExecutor.maybeExecute {
            connection.setRemoteDescription(remoteSdpObserver, sdp)
        }
    }

    @Suppress("NAME_SHADOWING")
    private fun onSetLocalDescriptionSuccess(sdp: SessionDescription) {
        signalingExecutor.maybeExecute {
            try {
                val sdp = SessionDescription(
                    SessionDescription.Type.ANSWER,
                    config.signaling.onOffer(
                        callType = "WEBRTC",
                        description = sdp.description,
                        presentationInMix = config.presentationInMain
                    )
                )
                setRemoteDescription(sdp)
            } catch (t: Throwable) {
                // noop
            }
        }
    }

    private fun onCandidate(candidate: String, mid: String) {
        signalingExecutor.maybeExecute {
            try {
                config.signaling.onCandidate(candidate, mid)
            } catch (t: Throwable) {
                // noop
            }
        }
    }

    private fun onVideoMuted() {
        signalingExecutor.maybeExecute {
            try {
                config.signaling.onVideoMuted()
            } catch (t: Throwable) {
                // noop
            }
        }
    }

    private fun onVideoUnmuted() {
        signalingExecutor.maybeExecute {
            try {
                config.signaling.onVideoUnmuted()
            } catch (t: Throwable) {
                // noop
            }
        }
    }

    private fun ExecutorService.maybeExecute(block: () -> Unit) {
        if (!isShutdown) execute(block)
    }

    private fun createPeerConnection() =
        checkNotNull(factory.factory.createPeerConnection(createRTCConfiguration(), observer))

    private fun createRTCConfiguration(): PeerConnection.RTCConfiguration {
        val iceServers = config.iceServers.map {
            PeerConnection.IceServer.builder(it.urls.toList())
                .setUsername(it.username)
                .setPassword(it.password)
                .createIceServer()
        }
        val c = PeerConnection.RTCConfiguration(iceServers)
        c.sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN
        c.continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY
        return c
    }

    public class Builder(signaling: MediaConnectionSignaling) {

        private val factory = WebRtcMediaConnectionFactory()
        private val builder = MediaConnectionConfig.Builder(signaling)

        public fun addIceServer(iceServer: PeerConnection.IceServer): Builder =
            addIceServers(listOf(iceServer))

        public fun addIceServers(iceServers: Collection<PeerConnection.IceServer>): Builder =
            apply {
                for (iceServer in iceServers) {
                    val i = IceServer.Builder(iceServer.urls)
                        .username(iceServer.username)
                        .password(iceServer.password)
                        .build()
                    this.builder.addIceServer(i)
                }
            }

        @Deprecated(
            message = "Renamed to presentationInMain.",
            replaceWith = ReplaceWith("this.presentationInMain(presentationInMix)"),
            level = DeprecationLevel.ERROR
        )
        public fun presentationInMix(presentationInMix: Boolean): Builder =
            presentationInMain(presentationInMix)

        public fun presentationInMain(presentationInMain: Boolean): Builder = apply {
            this.builder.presentationInMain(presentationInMain)
        }

        public fun mainQualityProfile(mainQualityProfile: QualityProfile): Builder = apply {
            this.builder.mainQualityProfile(mainQualityProfile)
        }

        @Deprecated("Use WebRtcMediaConnectionFactory.createMediaConnection() instead.")
        public fun build(): WebRtcMediaConnection = WebRtcMediaConnection(
            factory = factory,
            config = builder.build(),
            workerExecutor = Executors.newSingleThreadExecutor(),
            signalingExecutor = Executors.newSingleThreadExecutor(),
            shouldDisposeFactory = true
        )
    }

    public companion object {

        @Deprecated(
            message = "Use WebRtcMediaConnectionFactory.initialize(context) instead.",
            replaceWith = ReplaceWith(
                expression = "WebRtcMediaConnectionFactory.initialize(context)",
                imports = ["com.pexip.sdk.media.webrtc.WebRtcMediaConnectionFactory"]
            ),
            level = DeprecationLevel.ERROR
        )
        @JvmStatic
        public fun initialize(context: Context) {
            WebRtcMediaConnectionFactory.initialize(context)
        }
    }
}
