package com.pexip.sdk.sample.pinchallenge

import java.net.URL

data class PinChallengeProps(
    val node: URL,
    val conferenceAlias: String,
    val displayName: String,
    val required: Boolean,
)
