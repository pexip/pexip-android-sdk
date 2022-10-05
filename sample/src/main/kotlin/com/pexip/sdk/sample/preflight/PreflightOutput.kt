package com.pexip.sdk.sample.preflight

import com.pexip.sdk.api.infinity.RequestTokenResponse
import java.net.URL

sealed interface PreflightOutput {

    @JvmInline
    value class Toast(val message: String) : PreflightOutput

    data class Conference(
        val node: URL,
        val conferenceAlias: String,
        val presentationInMain: Boolean,
        val response: RequestTokenResponse,
    ) : PreflightOutput

    object Back : PreflightOutput {

        override fun toString(): String = "Back"
    }
}
