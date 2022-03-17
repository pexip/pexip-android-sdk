package com.pexip.sdk.video.conference.internal

import org.webrtc.SdpObserver
import org.webrtc.SessionDescription

internal interface SimpleSdpObserver : SdpObserver {

    override fun onCreateSuccess(description: SessionDescription) {
    }

    override fun onCreateFailure(reason: String) {
    }

    override fun onSetSuccess() {
    }

    override fun onSetFailure(reason: String) {
    }
}
