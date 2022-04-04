package com.pexip.sdk.video.sample.conference

import com.pexip.sdk.api.infinity.RequestTokenResponse
import java.net.URL

data class ConferenceProps(
    val node: URL,
    val conferenceAlias: String,
    val response: RequestTokenResponse,
)
