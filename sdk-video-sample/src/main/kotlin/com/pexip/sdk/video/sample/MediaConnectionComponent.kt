package com.pexip.sdk.video.sample

import com.pexip.sdk.media.MediaConnectionFactory
import com.pexip.sdk.media.webrtc.WebRtcMediaConnectionFactory
import org.webrtc.ContextUtils

object MediaConnectionComponent {

    val factory: MediaConnectionFactory by lazy {
        WebRtcMediaConnectionFactory(
            context = ContextUtils.getApplicationContext(),
            eglBase = EglBaseComponent.eglBase
        )
    }
}
