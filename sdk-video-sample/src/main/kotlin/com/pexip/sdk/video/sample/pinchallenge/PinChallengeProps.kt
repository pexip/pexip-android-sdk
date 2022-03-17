package com.pexip.sdk.video.sample.pinchallenge

import com.pexip.sdk.video.node.Node

data class PinChallengeProps(
    val alias: String,
    val node: Node,
    val displayName: String,
    val required: Boolean,
)
