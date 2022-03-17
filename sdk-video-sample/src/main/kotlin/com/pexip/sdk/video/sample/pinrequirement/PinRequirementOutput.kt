package com.pexip.sdk.video.sample.pinrequirement

import com.pexip.sdk.video.token.Token

sealed class PinRequirementOutput {

    data class Some(val required: Boolean) : PinRequirementOutput()

    data class None(val token: Token) : PinRequirementOutput()

    object Back : PinRequirementOutput() {

        override fun toString(): String = "Back"
    }
}
