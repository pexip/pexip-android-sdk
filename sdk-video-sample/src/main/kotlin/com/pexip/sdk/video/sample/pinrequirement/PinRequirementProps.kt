package com.pexip.sdk.video.sample.pinrequirement

import java.net.URL

data class PinRequirementProps(
    val node: URL,
    val conferenceAlias: String,
    val displayName: String,
)
