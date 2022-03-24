package com.pexip.sdk.video.sample.conference

import com.pexip.sdk.video.api.ConferenceAlias
import com.pexip.sdk.video.api.Node
import com.pexip.sdk.video.api.RequestTokenResponse

data class ConferenceProps(
    val node: Node,
    val conferenceAlias: ConferenceAlias,
    val response: RequestTokenResponse,
)
