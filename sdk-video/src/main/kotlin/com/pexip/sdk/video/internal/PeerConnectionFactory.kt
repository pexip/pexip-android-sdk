package com.pexip.sdk.video.internal

import java.util.concurrent.CopyOnWriteArraySet
import org.webrtc.PeerConnectionFactory as WebRtcPeerConnectionFactory

internal class PeerConnectionFactory(private val service: InfinityService) {

    private val factory = WebRtcPeerConnectionFactory.builder().createPeerConnectionFactory()
    private val connections = CopyOnWriteArraySet<PeerConnection>(mutableSetOf())

    fun createPeerConnection(): PeerConnection {
        val peerConnection = PeerConnection(service, factory)
        connections.add(peerConnection)
        return peerConnection
    }

    fun dispose() {
        connections.forEach { it.dispose() }
        connections.clear()
        factory.dispose()
    }
}
