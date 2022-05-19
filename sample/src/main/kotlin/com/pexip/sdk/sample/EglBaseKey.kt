package com.pexip.sdk.sample

import com.squareup.workflow1.ui.ViewEnvironmentKey
import org.webrtc.EglBase

object EglBaseKey : ViewEnvironmentKey<EglBase>(EglBase::class) {
    override val default: EglBase
        get() = error("Must be set.")
}
