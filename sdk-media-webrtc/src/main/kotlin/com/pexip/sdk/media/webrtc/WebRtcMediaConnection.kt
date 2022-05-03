package com.pexip.sdk.media.webrtc

import android.content.Context
import android.os.Looper
import androidx.core.os.HandlerCompat
import com.pexip.sdk.media.IceServer
import com.pexip.sdk.media.LocalAudioTrack
import com.pexip.sdk.media.LocalVideoTrack
import com.pexip.sdk.media.MediaConnection
import com.pexip.sdk.media.MediaConnectionConfig
import com.pexip.sdk.media.MediaConnectionSignaling
import com.pexip.sdk.media.QualityProfile
import com.pexip.sdk.media.webrtc.internal.SimplePeerConnectionObserver
import com.pexip.sdk.media.webrtc.internal.SimpleSdpObserver
import com.pexip.sdk.media.webrtc.internal.WebRtcLocalAudioTrack
import com.pexip.sdk.media.webrtc.internal.WebRtcLocalVideoTrack
import com.pexip.sdk.media.webrtc.internal.WebRtcVideoTrack
import com.pexip.sdk.media.webrtc.internal.mangle
import org.webrtc.EglBase
import org.webrtc.IceCandidate
import org.webrtc.MediaConstraints
import org.webrtc.MediaStreamTrack
import org.webrtc.PeerConnection
import org.webrtc.RtpTransceiver
import org.webrtc.RtpTransceiver.RtpTransceiverDirection
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription
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
    private val handler = HandlerCompat.createAsync(Looper.getMainLooper())
    private val observer = object : SimplePeerConnectionObserver {

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
    private val connection = factory.createPeerConnection(createRTCConfiguration(), observer)
    private var mainVideoTrack: LocalVideoTrack? = null

    @Volatile
    private var mainAudioTransceiver: RtpTransceiver? = null

    @Volatile
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
    private val mainRemoteVideoTrackListeners =
        CopyOnWriteArraySet<MediaConnection.RemoteVideoTrackListener>()
    private val presentationRemoteVideoTrackListeners =
        CopyOnWriteArraySet<MediaConnection.RemoteVideoTrackListener>()
    private val mainCapturingListener = LocalVideoTrack.CapturingListener {
        if (it) onVideoUnmuted() else onVideoMuted()
    }

    @Deprecated("Use WebRtcMediaConnectionFactory.eglBaseContext instead.")
    public val eglBaseContext: EglBase.Context
        get() = factory.eglBaseContext

    override fun sendMainAudio(localAudioTrack: LocalAudioTrack) {
        require(localAudioTrack is WebRtcLocalAudioTrack) { "localAudioTrack must be an instance of WebRtcLocalAudioTrack." }
        sendMainAudioInternal(localAudioTrack)
    }

    override fun sendMainVideo(localVideoTrack: LocalVideoTrack) {
        require(localVideoTrack is WebRtcLocalVideoTrack) { "localVideoTrack must be an instance of WebRtcLocalVideoTrack." }
        sendMainVideoInternal(localVideoTrack)
    }

    override fun startPresentationReceive() {
        workerExecutor.maybeExecute {
            val t = presentationVideoTransceiver
                ?.takeUnless { it.currentDirection == RtpTransceiverDirection.RECV_ONLY }
                ?: return@maybeExecute
            t.direction = RtpTransceiverDirection.RECV_ONLY
            val videoTrack = (t.receiver.track() as? VideoTrack)?.let(::WebRtcVideoTrack)
            presentationRemoteVideoTrackListeners.forEach {
                it.onRemoteVideoTrack(videoTrack)
            }
        }
    }

    override fun stopPresentationReceive() {
        workerExecutor.maybeExecute {
            val t = presentationVideoTransceiver
                ?.takeUnless { it.currentDirection == RtpTransceiverDirection.INACTIVE }
                ?: return@maybeExecute
            t.direction = RtpTransceiverDirection.INACTIVE
            presentationRemoteVideoTrackListeners.forEach {
                it.onRemoteVideoTrack(null)
            }
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
            mainVideoTrack?.unregisterCapturingListener(mainCapturingListener)
            mainVideoTrack = null
            mainRemoteVideoTrackListeners.clear()
            presentationRemoteVideoTrackListeners.clear()
            connection.dispose()
            if (shouldDisposeFactory) {
                factory.dispose()
            }
        }
        workerExecutor.shutdown()
        signalingExecutor.shutdown()
        handler.removeCallbacksAndMessages(null)
    }

    override fun registerMainRemoteVideoTrackListener(listener: MediaConnection.RemoteVideoTrackListener) {
        if (!workerExecutor.isShutdown) handler.post {
            val videoTrack = mainVideoTransceiver?.receiver?.track() as? VideoTrack
            listener.onRemoteVideoTrack(videoTrack?.let(::WebRtcVideoTrack))
        }
        mainRemoteVideoTrackListeners += listener
    }

    override fun unregisterMainRemoteVideoTrackListener(listener: MediaConnection.RemoteVideoTrackListener) {
        mainRemoteVideoTrackListeners -= listener
    }

    override fun registerPresentationRemoteVideoTrackListener(listener: MediaConnection.RemoteVideoTrackListener) {
        if (!workerExecutor.isShutdown) handler.post {
            val t = presentationVideoTransceiver
                ?.takeIf { it.currentDirection == RtpTransceiverDirection.RECV_ONLY }
                ?: return@post
            val videoTrack = t.receiver.track() as? VideoTrack
            listener.onRemoteVideoTrack(videoTrack?.let(::WebRtcVideoTrack))
        }
        presentationRemoteVideoTrackListeners += listener
    }

    override fun unregisterPresentationRemoteVideoTrackListener(listener: MediaConnection.RemoteVideoTrackListener) {
        presentationRemoteVideoTrackListeners -= listener
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

    private fun sendMainVideoInternal(localVideoTrack: WebRtcLocalVideoTrack) {
        workerExecutor.maybeExecute {
            if (mainVideoTransceiver == null) {
                mainVideoTrack = localVideoTrack
                mainVideoTrack?.registerCapturingListener(mainCapturingListener)
                val transceiver = connection.addTransceiver(
                    MediaStreamTrack.MediaType.MEDIA_TYPE_VIDEO,
                    RtpTransceiver.RtpTransceiverInit(RtpTransceiverDirection.SEND_RECV)
                )
                transceiver.sender.setTrack(localVideoTrack.videoTrack, false)
                mainVideoTransceiver = transceiver
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
