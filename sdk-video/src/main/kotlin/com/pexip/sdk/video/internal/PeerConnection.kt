package com.pexip.sdk.video.internal

import android.content.Context
import com.pexip.sdk.video.VideoTrackListener
import org.webrtc.CameraEnumerator
import org.webrtc.EglBase
import org.webrtc.IceCandidate
import org.webrtc.MediaConstraints
import org.webrtc.MediaConstraints.KeyValuePair
import org.webrtc.RtpTransceiver
import org.webrtc.RtpTransceiver.RtpTransceiverDirection
import org.webrtc.SessionDescription
import java.util.concurrent.atomic.AtomicBoolean
import org.webrtc.PeerConnection as WebRtcPeerConnection
import org.webrtc.PeerConnectionFactory as WebRtcPeerConnectionFactory

internal class PeerConnection private constructor(
    factory: WebRtcPeerConnectionFactory,
    rtcConfiguration: WebRtcPeerConnection.RTCConfiguration,
    private val signalingModule: SignalingModule,
    private val audioStrategy: AudioStrategy,
    private val videoStrategy: VideoStrategy,
) : Disposable, SimplePeerConnectionObserver {

    private val started = AtomicBoolean(false)
    private val disposed = AtomicBoolean(false)
    private val connection = checkNotNull(factory.createPeerConnection(rtcConfiguration, this))
    private val localSdpObserver = object : SimpleSdpObserver {

        override fun onCreateSuccess(description: SessionDescription) {
            Logger.log("localSdpObserver.onCreateSuccess(${description.type})")
            if (disposed.get()) return
            connection.setLocalDescription(this)
        }

        override fun onSetSuccess() {
            Logger.log("localSdpObserver.onSetSuccess()")
            signalingModule.onOffer(connection.localDescription.description) {
                if (disposed.get()) return@onOffer
                val answer = SessionDescription(SessionDescription.Type.ANSWER, it)
                connection.setRemoteDescription(remoteSdpObserver, answer)
            }
        }
    }
    private val remoteSdpObserver = object : SimpleSdpObserver {

        override fun onSetSuccess() {
            Logger.log("remoteSdpObserver.onSetSuccess()")
        }
    }

    init {
        audioStrategy.init(connection)
        videoStrategy.init(connection)
    }

    fun createOffer() {
        unlessDisposed {
            if (started.compareAndSet(false, true)) {
                val sdpConstraints = MediaConstraints()
                sdpConstraints.optional += KeyValuePair("DtlsSrtpKeyAgreement", "true")
                connection.createOffer(localSdpObserver, sdpConstraints)
            }
        }
    }

    fun startCapture() {
        unlessDisposed(videoStrategy::startCapture)
    }

    fun stopCapture() {
        unlessDisposed(videoStrategy::stopCapture)
    }

    override fun onConnectionChange(newState: WebRtcPeerConnection.PeerConnectionState) {
        if (newState == WebRtcPeerConnection.PeerConnectionState.CONNECTED) {
            signalingModule.onConnected()
        }
    }

    override fun onIceCandidate(candidate: IceCandidate) {
        signalingModule.onIceCandidate(candidate.sdp, candidate.sdpMid)
    }

    override fun onTrack(transceiver: RtpTransceiver) {
        videoStrategy.onTrack(transceiver)
    }

    fun registerLocalVideoTrackListener(listener: VideoTrackListener) {
        unlessDisposed { videoStrategy.registerLocalVideoTrackListener(listener) }
    }

    fun unregisterLocalVideoTrackListener(listener: VideoTrackListener) {
        videoStrategy.unregisterLocalVideoTrackListener(listener)
    }

    fun registerRemoteVideoTrackListener(listener: VideoTrackListener) {
        unlessDisposed { videoStrategy.registerRemoteVideoTrackListener(listener) }
    }

    fun unregisterRemoteVideoTrackListener(listener: VideoTrackListener) {
        videoStrategy.unregisterRemoteVideoTrackListener(listener)
    }

    override fun dispose() {
        if (disposed.compareAndSet(false, true)) {
            Logger.log("PeerConnection.dispose()")
            signalingModule.dispose()
            connection.dispose()
            audioStrategy.dispose()
            videoStrategy.dispose()
        }
    }

    private inline fun unlessDisposed(block: () -> Unit) {
        if (!disposed.get()) block()
    }

    class Builder(
        private val factory: WebRtcPeerConnectionFactory,
        private val rtcConfiguration: WebRtcPeerConnection.RTCConfiguration,
    ) {

        private var signalingModule: SignalingModule? = null
        private var audioStrategy: AudioStrategy = InactiveAudioStrategy
        private var videoStrategy: VideoStrategy = InactiveVideoStrategy

        fun signaling(service: InfinityService) = apply {
            this.signalingModule = SignalingModule(service)
        }

        fun sendReceiveAudio() = apply {
            this.audioStrategy = factory.createAudioStrategy(RtpTransceiverDirection.SEND_RECV)
        }

        fun sendReceiveVideo(
            enumerator: CameraEnumerator,
            sharedContext: EglBase.Context,
            applicationContext: Context,
        ) = apply {
            this.videoStrategy = factory.createVideoStrategy(
                direction = RtpTransceiverDirection.SEND_RECV,
                enumerator = enumerator,
                sharedContext = sharedContext,
                applicationContext = applicationContext
            )
        }

        fun build(): PeerConnection {
            val signalingModule = checkNotNull(signalingModule) { "signalingModule is not set." }
            return PeerConnection(
                factory = factory,
                rtcConfiguration = rtcConfiguration,
                signalingModule = signalingModule,
                audioStrategy = audioStrategy,
                videoStrategy = videoStrategy
            )
        }
    }
}
