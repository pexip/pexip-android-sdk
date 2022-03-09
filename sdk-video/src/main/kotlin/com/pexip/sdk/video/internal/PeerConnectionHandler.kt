package com.pexip.sdk.video.internal

import org.webrtc.RtpTransceiver.RtpTransceiverDirection
import org.webrtc.PeerConnection as WebRtcPeerConnection
import org.webrtc.PeerConnectionFactory as WebRtcPeerConnectionFactory

internal class PeerConnectionHandler(service: InfinityService) {

    private val peerConnectionFactory =
        WebRtcPeerConnectionFactory.builder().createPeerConnectionFactory()
    private val rtcConfiguration = object : WebRtcPeerConnection.RTCConfiguration(emptyList()) {

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
    private val peerConnection = PeerConnection.Builder(peerConnectionFactory, service)
        .rtcConfiguration(rtcConfiguration)
        .audio(RtpTransceiverDirection.SEND_RECV)
        .build()

    init {
        peerConnection.createOffer()
    }

    fun dispose() {
        peerConnection.dispose()
        peerConnectionFactory.dispose()
    }
}
