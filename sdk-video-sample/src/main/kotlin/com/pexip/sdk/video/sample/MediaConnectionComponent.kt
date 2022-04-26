package com.pexip.sdk.video.sample

import com.pexip.sdk.media.webrtc.WebRtcMediaConnectionFactory

object MediaConnectionComponent {

    val factory by lazy { WebRtcMediaConnectionFactory() }
}
