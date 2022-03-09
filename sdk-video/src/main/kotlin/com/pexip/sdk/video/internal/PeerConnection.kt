package com.pexip.sdk.video.internal

import org.webrtc.AudioSource
import org.webrtc.AudioTrack
import org.webrtc.IceCandidate
import org.webrtc.MediaConstraints
import org.webrtc.MediaStreamTrack
import org.webrtc.RtpTransceiver
import org.webrtc.SessionDescription
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import org.webrtc.PeerConnection as WebRtcPeerConnection
import org.webrtc.PeerConnectionFactory as WebRtcPeerConnectionFactory

internal class PeerConnection private constructor(
    private val factory: WebRtcPeerConnectionFactory,
    rtcConfiguration: WebRtcPeerConnection.RTCConfiguration,
    audioDirection: RtpTransceiver.RtpTransceiverDirection,
    service: InfinityService,
) {

    private val callId = AtomicReference<String>()
    private val started = AtomicBoolean(false)
    private val disposed = AtomicBoolean(false)
    private val executor = Executors.newSingleThreadExecutor()

    private val observer = object : SimplePeerConnectionObserver {

        override fun onConnectionChange(newState: WebRtcPeerConnection.PeerConnectionState) {
            Logger.log("onConnectionChange($newState)")
            if (newState == WebRtcPeerConnection.PeerConnectionState.CONNECTED) {
                executor.maybeSubmit {
                    val request = AckRequest(callId.get())
                    service.ack(request)
                }
            }
        }

        override fun onIceCandidate(candidate: IceCandidate) {
            Logger.log("onIceCandidate($candidate)")
            executor.maybeSubmit {
                val request = CandidateRequest(
                    callId = callId.get(),
                    candidate = candidate.sdp,
                    mid = candidate.sdpMid
                )
                service.newCandidate(request)
            }
        }
    }
    private val localSdpObserver = object : SimpleSdpObserver {

        override fun onCreateSuccess(description: SessionDescription) {
            Logger.log("localSdpObserver.onCreateSuccess(${description.type})")
            peerConnection.setLocalDescription(this)
        }

        override fun onSetSuccess() {
            Logger.log("localSdpObserver.onSetSuccess()")
            executor.maybeSubmit {
                val request = CallsRequest(peerConnection.localDescription.description)
                val response = service.calls(request)
                callId.set(response.call_uuid)
                val description = SessionDescription(SessionDescription.Type.ANSWER, response.sdp)
                peerConnection.setRemoteDescription(remoteSdpObserver, description)
            }
        }
    }
    private val remoteSdpObserver = object : SimpleSdpObserver {

        override fun onSetSuccess() {
            Logger.log("remoteSdpObserver.onSetSuccess()")
        }
    }

    private val peerConnection = factory.createPeerConnection(rtcConfiguration, observer)!!
    private val audioSource: AudioSource?
    private val audioTrack: AudioTrack?

    init {
        when (audioDirection) {
            RtpTransceiver.RtpTransceiverDirection.SEND_RECV, RtpTransceiver.RtpTransceiverDirection.SEND_ONLY -> {
                val audioConstrains = MediaConstraints()
                audioSource = factory.createAudioSource(audioConstrains)
                audioTrack = factory.createAudioTrack(AUDIO_TRACK_ID, audioSource)
                val init = RtpTransceiver.RtpTransceiverInit(audioDirection)
                peerConnection.addTransceiver(audioTrack, init)
            }
            RtpTransceiver.RtpTransceiverDirection.RECV_ONLY -> {
                audioSource = null
                audioTrack = null
                val init = RtpTransceiver.RtpTransceiverInit(audioDirection)
                peerConnection.addTransceiver(MediaStreamTrack.MediaType.MEDIA_TYPE_AUDIO, init)
            }
            RtpTransceiver.RtpTransceiverDirection.INACTIVE -> {
                audioSource = null
                audioTrack = null
            }
        }
    }

    fun createOffer() {
        if (disposed.get()) return
        if (started.compareAndSet(false, true)) {
            val sdpConstraints = MediaConstraints()
            sdpConstraints.optional += MediaConstraints.KeyValuePair("DtlsSrtpKeyAgreement", "true")
            peerConnection.createOffer(localSdpObserver, sdpConstraints)
        }
    }

    fun dispose() {
        if (disposed.compareAndSet(false, true)) {
            Logger.log("PeerConnection.dispose()")
            executor.shutdownNow()
            audioTrack?.dispose()
            audioSource?.dispose()
            peerConnection.dispose()
        }
    }

    class Builder(
        private val factory: WebRtcPeerConnectionFactory,
        private val service: InfinityService,
    ) {

        private var rtcConfiguration: WebRtcPeerConnection.RTCConfiguration? = null
        private var audioDirection = RtpTransceiver.RtpTransceiverDirection.INACTIVE

        fun rtcConfiguration(rtcConfiguration: WebRtcPeerConnection.RTCConfiguration) = apply {
            this.rtcConfiguration = rtcConfiguration
        }

        fun audio(direction: RtpTransceiver.RtpTransceiverDirection) = apply {
            this.audioDirection = direction
        }

        fun build() = PeerConnection(
            factory = factory,
            rtcConfiguration = checkNotNull(rtcConfiguration) { "rtcConfiguration is not set." },
            audioDirection = audioDirection,
            service = service
        )
    }

    private companion object {

        const val AUDIO_TRACK_ID = "ARDAMSa0"
    }
}
