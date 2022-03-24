package com.pexip.sdk.video.sample.pinrequirement

import com.pexip.sdk.video.api.ConferenceAlias
import com.pexip.sdk.video.api.Node

data class PinRequirementProps(
    val node: Node,
    val conferenceAlias: ConferenceAlias,
    val displayName: String,
)
