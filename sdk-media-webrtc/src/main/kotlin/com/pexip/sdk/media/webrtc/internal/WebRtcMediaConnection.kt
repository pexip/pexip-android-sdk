package com.pexip.sdk.media.webrtc.internal

import com.pexip.sdk.media.LocalAudioTrack
import com.pexip.sdk.media.LocalMediaTrack
import com.pexip.sdk.media.LocalVideoTrack
import com.pexip.sdk.media.MediaConnection
import com.pexip.sdk.media.MediaConnectionConfig
import com.pexip.sdk.media.webrtc.WebRtcMediaConnectionFactory
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
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.properties.Delegates

internal class WebRtcMediaConnection(
    factory: WebRtcMediaConnectionFactory,
    private val config: MediaConnectionConfig,
    private val workerExecutor: ExecutorService,
    private val signalingExecutor: ExecutorService,
) : MediaConnection {

    private val started = AtomicBoolean()
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

    // Rename due to platform declaration clash
    @set:JvmName("setMainAudioTrackInternal")
    private var mainAudioTrack: LocalAudioTrack? by Delegates.observable(null) { _, old, new ->
        old?.unregisterCapturingListener(mainAudioTrackCapturingListener)
        new?.registerCapturingListener(mainAudioTrackCapturingListener)
    }

    // Rename due to platform declaration clash
    @set:JvmName("setMainVideoTrackInternal")
    private var mainVideoTrack: LocalVideoTrack? by Delegates.observable(null) { _, old, new ->
        old?.unregisterCapturingListener(mainVideoTrackCapturingListener)
        new?.registerCapturingListener(mainVideoTrackCapturingListener)
    }

    private var mainAudioTransceiver: RtpTransceiver? = null
    private var mainVideoTransceiver: RtpTransceiver? = null
    private val presentationVideoTransceiver = connection.addTransceiver(
        MediaStreamTrack.MediaType.MEDIA_TYPE_VIDEO,
        RtpTransceiver.RtpTransceiverInit(RtpTransceiverDirection.INACTIVE)
    )
    private val localSdpObserver = object : SimpleSdpObserver {

        override fun onCreateSuccess(description: SessionDescription) {
            setLocalDescription(this)
        }

        override fun onSetSuccess() {
            val mangledDescription = connection.localDescription.mangle(
                mainAudioMid = mainAudioTransceiver?.mid,
                mainVideoMid = mainVideoTransceiver?.mid,
                presentationVideoMid = presentationVideoTransceiver.mid
            )
            onSetLocalDescriptionSuccess(mangledDescription)
        }
    }
    private val remoteSdpObserver = object : SimpleSdpObserver {}
    private val mainRemoteVideoTrackListeners =
        CopyOnWriteArraySet<MediaConnection.RemoteVideoTrackListener>()
    private val presentationRemoteVideoTrackListeners =
        CopyOnWriteArraySet<MediaConnection.RemoteVideoTrackListener>()
    private val mainAudioTrackCapturingListener = LocalMediaTrack.CapturingListener {
        if (it) onAudioUnmuted() else onAudioMuted()
    }
    private val mainVideoTrackCapturingListener = LocalMediaTrack.CapturingListener {
        if (it) onVideoUnmuted() else onVideoMuted()
    }

    override fun sendMainAudio(localAudioTrack: LocalAudioTrack) {
        setMainAudioTrack(localAudioTrack)
    }

    override fun sendMainVideo(localVideoTrack: LocalVideoTrack) {
        setMainVideoTrack(localVideoTrack)
    }

    override fun setMainAudioTrack(localAudioTrack: LocalAudioTrack?) {
        val lat = when (localAudioTrack) {
            is WebRtcLocalAudioTrack -> localAudioTrack
            null -> null
            else -> throw IllegalArgumentException("localAudioTrack must be null or an instance of WebRtcLocalAudioTrack.")
        }
        workerExecutor.maybeExecute {
            val transceiver = when (val transceiver = mainAudioTransceiver) {
                null -> connection.addTransceiver(MediaStreamTrack.MediaType.MEDIA_TYPE_AUDIO)
                else -> transceiver
            }
            transceiver.sender.setTrack(lat?.audioTrack, false)
            mainAudioTransceiver = transceiver
            mainAudioTrack = lat
        }
    }

    override fun setMainVideoTrack(localVideoTrack: LocalVideoTrack?) {
        val lvt = when (localVideoTrack) {
            is WebRtcLocalVideoTrack -> localVideoTrack
            null -> null
            else -> throw IllegalArgumentException("localVideoTrack must be null or an instance of WebRtcLocalVideoTrack.")
        }
        workerExecutor.maybeExecute {
            val transceiver = when (val transceiver = mainVideoTransceiver) {
                null -> connection.addTransceiver(MediaStreamTrack.MediaType.MEDIA_TYPE_VIDEO)
                else -> transceiver
            }
            transceiver.sender.setTrack(lvt?.videoTrack, false)
            mainVideoTransceiver = transceiver
            mainVideoTrack = lvt
        }
    }

    override fun startPresentationReceive() {
        if (!config.presentationInMain) workerExecutor.maybeExecute {
            val t = presentationVideoTransceiver
                .takeUnless { it.currentDirection == RtpTransceiverDirection.SEND_RECV }
                ?: return@maybeExecute
            t.direction = RtpTransceiverDirection.SEND_RECV
            val videoTrack = (t.receiver.track() as? VideoTrack)?.let(::WebRtcVideoTrack)
            presentationRemoteVideoTrackListeners.forEach {
                it.onRemoteVideoTrack(videoTrack)
            }
        }
    }

    override fun stopPresentationReceive() {
        if (!config.presentationInMain) workerExecutor.maybeExecute {
            val t = presentationVideoTransceiver
                .takeUnless { it.currentDirection == RtpTransceiverDirection.INACTIVE }
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
            mainAudioTrack = null
            mainVideoTrack = null
            mainAudioTransceiver?.sender?.setTrack(null, false)
            mainVideoTransceiver?.sender?.setTrack(null, false)
            presentationVideoTransceiver.sender.setTrack(null, false)
            mainRemoteVideoTrackListeners.clear()
            presentationRemoteVideoTrackListeners.clear()
            connection.dispose()
        }
        workerExecutor.shutdown()
        signalingExecutor.shutdown()
    }

    override fun registerMainRemoteVideoTrackListener(listener: MediaConnection.RemoteVideoTrackListener) {
        workerExecutor.maybeExecute {
            val videoTrack = mainVideoTransceiver?.receiver?.track() as? VideoTrack
            listener.onRemoteVideoTrack(videoTrack?.let(::WebRtcVideoTrack))
        }
        mainRemoteVideoTrackListeners += listener
    }

    override fun unregisterMainRemoteVideoTrackListener(listener: MediaConnection.RemoteVideoTrackListener) {
        mainRemoteVideoTrackListeners -= listener
    }

    override fun registerPresentationRemoteVideoTrackListener(listener: MediaConnection.RemoteVideoTrackListener) {
        if (!config.presentationInMain) workerExecutor.maybeExecute {
            val t = presentationVideoTransceiver
                .takeIf { it.currentDirection == RtpTransceiverDirection.SEND_RECV }
                ?: return@maybeExecute
            val videoTrack = t.receiver.track() as? VideoTrack
            listener.onRemoteVideoTrack(videoTrack?.let(::WebRtcVideoTrack))
        }
        presentationRemoteVideoTrackListeners += listener
    }

    override fun unregisterPresentationRemoteVideoTrackListener(listener: MediaConnection.RemoteVideoTrackListener) {
        presentationRemoteVideoTrackListeners -= listener
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

    private fun onAudioMuted() {
        signalingExecutor.maybeExecute {
            try {
                config.signaling.onAudioMuted()
            } catch (t: Throwable) {
                // noop
            }
        }
    }

    private fun onAudioUnmuted() {
        signalingExecutor.maybeExecute {
            try {
                config.signaling.onAudioUnmuted()
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
        c.enableDscp = config.dscp
        c.sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN
        c.continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY
        return c
    }
}
