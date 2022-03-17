package com.pexip.sdk.video.sample.pinrequirement

import com.pexip.sdk.video.node.Node

data class PinRequirementProps(
    val alias: String,
    val node: Node,
    val displayName: String,
)
