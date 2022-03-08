package com.pexip.sdk.video.internal

internal class PeerConnectionHandler(service: InfinityService) {

    private val factory = PeerConnectionFactory(service)
    private val peerConnection = factory.createPeerConnection()

    fun dispose() {
        factory.dispose()
    }
}
