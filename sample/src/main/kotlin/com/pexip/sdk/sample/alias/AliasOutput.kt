package com.pexip.sdk.sample.alias

import com.pexip.sdk.api.infinity.RequestTokenResponse
import java.net.URL

sealed class AliasOutput {

    data class Conference(
        val node: URL,
        val conferenceAlias: String,
        val presentationInMain: Boolean,
        val response: RequestTokenResponse,
    ) : AliasOutput()

    data class PinChallenge(
        val node: URL,
        val conferenceAlias: String,
        val presentationInMain: Boolean,
        val required: Boolean,
    ) : AliasOutput()

    data class Toast(val message: String) : AliasOutput()

    object Back : AliasOutput()
}
