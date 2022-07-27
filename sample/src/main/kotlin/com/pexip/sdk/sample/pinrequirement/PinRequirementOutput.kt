package com.pexip.sdk.sample.pinrequirement

import com.pexip.sdk.api.infinity.RequestTokenResponse
import java.net.URL

sealed class PinRequirementOutput {

    data class Some(val node: URL, val required: Boolean) : PinRequirementOutput()

    data class None(val node: URL, val response: RequestTokenResponse) : PinRequirementOutput()

    object Back : PinRequirementOutput() {

        override fun toString(): String = "Back"
    }
}
