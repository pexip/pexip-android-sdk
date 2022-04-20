package com.pexip.sdk.media.webrtc

import android.content.Context
import com.pexip.sdk.media.CapturingListener
import com.pexip.sdk.media.MediaConnection
import com.pexip.sdk.media.MediaConnectionSignaling
import com.pexip.sdk.media.QualityProfile
import com.pexip.sdk.media.webrtc.internal.SimplePeerConnectionObserver
import com.pexip.sdk.media.webrtc.internal.SimpleSdpObserver
import com.pexip.sdk.media.webrtc.internal.mangle
import org.webrtc.AudioSource
import org.webrtc.AudioTrack
import org.webrtc.Camera1Enumerator
import org.webrtc.Camera2Enumerator
import org.webrtc.CameraEnumerator
import org.webrtc.CameraVideoCapturer
import org.webrtc.ContextUtils
import org.webrtc.DefaultVideoDecoderFactory
import org.webrtc.DefaultVideoEncoderFactory
import org.webrtc.EglBase
import org.webrtc.IceCandidate
import org.webrtc.MediaConstraints
import org.webrtc.MediaStreamTrack
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
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

public class WebRtcMediaConnection private constructor(
    configuration: PeerConnection.RTCConfiguration,
    private val context: Context,
    private val eglBase: EglBase,
    private val factory: PeerConnectionFactory,
    private val workerExecutor: ExecutorService,
    private val signalingExecutor: ExecutorService,
    private val signaling: MediaConnectionSignaling,
    private val presentationInMain: Boolean,
    private val cameraEnumerator: CameraEnumerator,
    private val mainQualityProfile: QualityProfile,
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
    private val connection = checkNotNull(factory.createPeerConnection(configuration, observer))
    private var mainAudioSource: AudioSource? = null
    private var mainAudioTrack: AudioTrack? = null
    private var mainVideoCapturer: CameraVideoCapturer? = null
    private var mainVideoCapturing: Boolean = false
    private var mainVideoSource: VideoSource? = null
    private var mainVideoTrack: VideoTrack? = null
    private var mainVideoSurfaceTextureHelper: SurfaceTextureHelper? = null
    private var mainAudioTransceiver: RtpTransceiver? = null
    private var mainVideoTransceiver: RtpTransceiver? = null
    private val presentationVideoTransceiver: RtpTransceiver? = when (presentationInMain) {
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

    public val eglBaseContext: EglBase.Context
        get() = eglBase.eglBaseContext

    override fun sendMainAudio() {
        sendMainAudioInternal()
    }

    override fun sendMainVideo() {
        sendMainVideoInternal()
    }

    override fun sendMainVideo(deviceName: String) {
        require(deviceName in cameraEnumerator.deviceNames) { "deviceName is not available." }
        sendMainVideoInternal(deviceName)
    }

    override fun startMainCapture() {
        workerExecutor.maybeExecute {
            val capturer = mainVideoCapturer ?: return@maybeExecute
            capturer.startCapture(
                mainQualityProfile.width,
                mainQualityProfile.height,
                mainQualityProfile.fps
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
            connection.dispose()
            mainAudioTrack?.dispose()
            mainAudioSource?.dispose()
            mainVideoTrack?.dispose()
            mainVideoCapturer?.stopCapture()
            mainVideoSurfaceTextureHelper?.dispose()
            mainVideoSource?.dispose()
            mainVideoCapturer?.dispose()
            factory.dispose()
            eglBase.release()
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

    private fun sendMainAudioInternal() {
        workerExecutor.maybeExecute {
            if (mainAudioTransceiver == null) {
                mainAudioSource = factory.createAudioSource(MediaConstraints())
                mainAudioTrack = factory.createAudioTrack("ARDAMSa0", mainAudioSource)
                mainAudioTransceiver = connection.addTransceiver(mainAudioTrack)
            }
        }
    }

    @Suppress("NAME_SHADOWING")
    private fun sendMainVideoInternal(deviceName: String? = null) {
        workerExecutor.maybeExecute {
            if (mainVideoTransceiver == null) {
                val deviceNames = cameraEnumerator.deviceNames
                val deviceName = deviceName
                    ?: deviceNames.firstOrNull(cameraEnumerator::isFrontFacing)
                    ?: deviceNames.firstOrNull(cameraEnumerator::isBackFacing)
                    ?: deviceNames.first()
                mainVideoCapturer = cameraEnumerator.createCapturer(deviceName, null)
                mainVideoSource = factory.createVideoSource(mainVideoCapturer!!.isScreencast)
                mainVideoTrack = factory.createVideoTrack("ARDAMSv0", mainVideoSource)
                val transceiver = connection.addTransceiver(mainVideoTrack)
                mainVideoTransceiver = transceiver
                mainLocalVideoTrackListeners.forEach {
                    it.onVideoTrack(transceiver.sender.track() as? VideoTrack)
                }
                mainRemoteVideoTrackListeners.forEach {
                    it.onVideoTrack(transceiver.receiver.track() as? VideoTrack)
                }
                mainVideoSurfaceTextureHelper = SurfaceTextureHelper.create("main", eglBaseContext)
                mainVideoCapturer?.initialize(
                    mainVideoSurfaceTextureHelper,
                    context,
                    mainVideoSource?.capturerObserver
                )
            }
        }
    }

    private fun createOffer() {
        workerExecutor.maybeExecute {
            val sdpConstraints = MediaConstraints()
            sdpConstraints.optional += MediaConstraints.KeyValuePair("DtlsSrtpKeyAgreement", "true")
            connection.createOffer(localSdpObserver, sdpConstraints)
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
                    signaling.onOffer(
                        callType = "WEBRTC",
                        description = sdp.description,
                        presentationInMix = presentationInMain
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
                signaling.onCandidate(candidate, mid)
            } catch (t: Throwable) {
                // noop
            }
        }
    }

    private fun onVideoMuted() {
        signalingExecutor.maybeExecute {
            try {
                signaling.onVideoMuted()
            } catch (t: Throwable) {
                // noop
            }
        }
    }

    private fun onVideoUnmuted() {
        signalingExecutor.maybeExecute {
            try {
                signaling.onVideoUnmuted()
            } catch (t: Throwable) {
                // noop
            }
        }
    }

    private fun ExecutorService.maybeExecute(block: () -> Unit) {
        if (!isShutdown) execute(block)
    }

    public class Builder(private val signaling: MediaConnectionSignaling) {

        private val iceServers = mutableSetOf<PeerConnection.IceServer>()

        private var presentationInMain = false
        private var mainQualityProfile = QualityProfile.Medium

        public fun addIceServer(iceServer: PeerConnection.IceServer): Builder = apply {
            this.iceServers += iceServer
        }

        public fun addIceServers(iceServers: Collection<PeerConnection.IceServer>): Builder =
            apply {
                this.iceServers += iceServers
            }

        @Deprecated(
            message = "Renamed to presentationInMain.",
            replaceWith = ReplaceWith("this.presentationInMain(presentationInMix)"),
            level = DeprecationLevel.ERROR
        )
        public fun presentationInMix(presentationInMix: Boolean): Builder =
            presentationInMain(presentationInMix)

        public fun presentationInMain(presentationInMain: Boolean): Builder = apply {
            this.presentationInMain = presentationInMain
        }

        public fun mainQualityProfile(qualityProfile: QualityProfile): Builder = apply {
            this.mainQualityProfile = qualityProfile
        }

        public fun build(): WebRtcMediaConnection {
            val configuration = PeerConnection.RTCConfiguration(iceServers.toList())
            configuration.sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN
            configuration.continualGatheringPolicy =
                PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY
            val eglBase = EglBase.create()
            val decoderFactory = DefaultVideoDecoderFactory(eglBase.eglBaseContext)
            val encoderFactory = DefaultVideoEncoderFactory(eglBase.eglBaseContext, false, false)
            val context = ContextUtils.getApplicationContext()
            return WebRtcMediaConnection(
                configuration = configuration,
                context = context,
                eglBase = eglBase,
                factory = PeerConnectionFactory.builder()
                    .setVideoDecoderFactory(decoderFactory)
                    .setVideoEncoderFactory(encoderFactory)
                    .createPeerConnectionFactory(),
                workerExecutor = Executors.newSingleThreadExecutor(),
                signalingExecutor = Executors.newSingleThreadExecutor(),
                signaling = signaling,
                presentationInMain = presentationInMain,
                cameraEnumerator = CameraEnumerator(context),
                mainQualityProfile = mainQualityProfile
            )
        }

        private fun CameraEnumerator(c: Context) = when (Camera2Enumerator.isSupported(c)) {
            true -> Camera2Enumerator(c)
            else -> Camera1Enumerator()
        }
    }

    public companion object {

        @JvmStatic
        public fun initialize(context: Context) {
            val applicationContext = context.applicationContext
            val options = PeerConnectionFactory.InitializationOptions.builder(applicationContext)
                .createInitializationOptions()
            PeerConnectionFactory.initialize(options)
        }
    }
}
