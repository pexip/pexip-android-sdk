package com.pexip.sdk.sample.conference

import com.pexip.sdk.api.infinity.RequestTokenResponse
import java.net.URL

data class ConferenceProps(
    val node: URL,
    val conferenceAlias: String,
    val presentationInMain: Boolean,
    val response: RequestTokenResponse,
)
