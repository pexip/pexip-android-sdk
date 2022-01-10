package com.pexip.sdk.sample.pinrequirement

import com.pexip.sdk.api.infinity.RequestTokenResponse

sealed class PinRequirementOutput {

    data class Some(val required: Boolean) : PinRequirementOutput()

    data class None(val response: RequestTokenResponse) : PinRequirementOutput()

    object Back : PinRequirementOutput() {

        override fun toString(): String = "Back"
    }
}
