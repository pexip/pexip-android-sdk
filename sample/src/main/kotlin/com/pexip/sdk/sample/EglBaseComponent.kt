package com.pexip.sdk.sample

import org.webrtc.EglBase

object EglBaseComponent {

    val eglBase: EglBase by lazy(EglBase::create)
}
