package com.pexip.sdk.video.conference.internal

import org.webrtc.PeerConnection

internal interface MediaStrategy : Disposable {

    fun init(connection: PeerConnection) {
        // noop
    }

    override fun dispose() {
        // noop
    }
}
