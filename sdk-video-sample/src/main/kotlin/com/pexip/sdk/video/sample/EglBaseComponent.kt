package com.pexip.sdk.video.sample

import org.webrtc.EglBase

object EglBaseComponent {

    val eglBase: EglBase by lazy(EglBase::create)
}
