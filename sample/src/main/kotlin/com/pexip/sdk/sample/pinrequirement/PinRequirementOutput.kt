package com.pexip.sdk.sample.pinrequirement

import com.pexip.sdk.api.infinity.RequestTokenResponse
import java.net.URL

sealed class PinRequirementOutput {

    data class None(
        val node: URL,
        val conferenceAlias: String,
        val response: RequestTokenResponse,
    ) : PinRequirementOutput()

    data class Some(
        val node: URL,
        val conferenceAlias: String,
        val required: Boolean,
    ) : PinRequirementOutput()

    data class Error(val t: Throwable) : PinRequirementOutput()
}
