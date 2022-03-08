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

internal class PeerConnection(
    service: InfinityService,
    factory: WebRtcPeerConnectionFactory,
    rtcConfiguration: WebRtcPeerConnection.RTCConfiguration = RtcConfiguration(),
) {

    private var callId = AtomicReference<String>()
    private val disposed = AtomicBoolean(false)
    private val executor = Executors.newSingleThreadExecutor()

    private val observer = object : SimplePeerConnectionObserver {

        override fun onConnectionChange(newState: WebRtcPeerConnection.PeerConnectionState) {
            Logger.log("onConnectionChange($newState)")
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
                service.ack(AckRequest(response.call_uuid))
                callId.set(response.call_uuid)
                val description = SessionDescription(
                    SessionDescription.Type.ANSWER,
                    response.sdp
                )
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
    private val audioSource: AudioSource
    private val audioTrack: AudioTrack

    init {
        val audioConstraints = MediaConstraints()
        audioSource = factory.createAudioSource(audioConstraints)
        audioTrack = factory.createAudioTrack("ARDAMSa0", audioSource)
        peerConnection.maybeAddTransceiver(audioTrack, true)
        val videoConstraints = MediaConstraints().apply {
            optional += MediaConstraints.KeyValuePair("DtlsSrtpKeyAgreement", "true")
        }
        peerConnection.createOffer(localSdpObserver, videoConstraints)
    }

    fun dispose() {
        if (disposed.compareAndSet(false, true)) {
            Logger.log("PeerConnection.dispose()")
            executor.shutdownNow()
            audioTrack.dispose()
            audioSource.dispose()
            peerConnection.dispose()
        }
    }

    private fun WebRtcPeerConnection.maybeAddTransceiver(track: AudioTrack?, receive: Boolean) =
        maybeAddTransceiver(
            track = track,
            type = MediaStreamTrack.MediaType.MEDIA_TYPE_AUDIO,
            receive = receive
        )

    private fun WebRtcPeerConnection.maybeAddTransceiver(
        track: MediaStreamTrack?,
        type: MediaStreamTrack.MediaType,
        receive: Boolean,
    ): RtpTransceiver? {
        val direction = when {
            track != null && receive -> RtpTransceiver.RtpTransceiverDirection.SEND_RECV
            track != null -> RtpTransceiver.RtpTransceiverDirection.SEND_ONLY
            receive -> RtpTransceiver.RtpTransceiverDirection.RECV_ONLY
            else -> return null
        }
        val init = RtpTransceiver.RtpTransceiverInit(direction)
        return when (track) {
            null -> addTransceiver(type, init)
            else -> addTransceiver(track, init)
        }
    }

    class RtcConfiguration : WebRtcPeerConnection.RTCConfiguration(emptyList()) {

        init {
            val urls = listOf(
                "stun:stun.l.google.com:19302",
                "stun:stun1.l.google.com:19302",
                "stun:stun2.l.google.com:19302",
                "stun:stun3.l.google.com:19302",
                "stun:stun4.l.google.com:19302"
            )
            val server = WebRtcPeerConnection.IceServer.builder(urls).createIceServer()
            iceServers = listOf(server)
            sdpSemantics = WebRtcPeerConnection.SdpSemantics.UNIFIED_PLAN
            continualGatheringPolicy =
                WebRtcPeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY
        }
    }
}
