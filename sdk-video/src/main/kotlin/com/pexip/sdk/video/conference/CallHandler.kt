package com.pexip.sdk.video.conference

import com.pexip.sdk.video.conference.internal.InfinityService
import com.pexip.sdk.video.conference.internal.PeerConnection
import org.webrtc.Camera1Enumerator
import org.webrtc.Camera2Enumerator
import org.webrtc.ContextUtils
import org.webrtc.DefaultVideoDecoderFactory
import org.webrtc.DefaultVideoEncoderFactory
import org.webrtc.EglBase
import org.webrtc.PeerConnection as WebRtcPeerConnection
import org.webrtc.PeerConnectionFactory as WebRtcPeerConnectionFactory

public class CallHandler internal constructor(service: InfinityService) {

    private val eglBase = EglBase.create()
    private val sharedContext = eglBase.eglBaseContext
    private val applicationContext = ContextUtils.getApplicationContext()
    private val cameraEnumerator = when (Camera2Enumerator.isSupported(applicationContext)) {
        true -> Camera2Enumerator(applicationContext)
        else -> Camera1Enumerator()
    }
    private val peerConnectionFactory = WebRtcPeerConnectionFactory.builder()
        .setVideoDecoderFactory(DefaultVideoDecoderFactory(sharedContext))
        .setVideoEncoderFactory(DefaultVideoEncoderFactory(sharedContext, false, false))
        .createPeerConnectionFactory()
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
    private val peerConnection =
        PeerConnection.Builder(peerConnectionFactory, rtcConfiguration)
            .signaling(service)
            .sendReceiveAudio()
            .sendReceiveVideo(
                enumerator = cameraEnumerator,
                sharedContext = sharedContext,
                applicationContext = applicationContext
            )
            .build()

    init {
        peerConnection.startCapture()
        peerConnection.createOffer()
    }

    internal fun dispose() {
        peerConnection.dispose()
        peerConnectionFactory.dispose()
        eglBase.release()
    }

    public fun registerLocalVideoTrackListener(listener: VideoTrackListener) {
        peerConnection.registerLocalVideoTrackListener(listener)
    }

    public fun unregisterLocalVideoTrackListener(listener: VideoTrackListener) {
        peerConnection.unregisterLocalVideoTrackListener(listener)
    }

    public fun registerRemoteVideoTrackListener(listener: VideoTrackListener) {
        peerConnection.registerRemoteVideoTrackListener(listener)
    }

    public fun unregisterRemoteVideoTrackListener(listener: VideoTrackListener) {
        peerConnection.unregisterRemoteVideoTrackListener(listener)
    }
}
