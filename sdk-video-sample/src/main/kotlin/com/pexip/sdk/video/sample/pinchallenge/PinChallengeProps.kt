package com.pexip.sdk.video.sample.pinchallenge

import com.pexip.sdk.video.JoinDetails
import com.pexip.sdk.video.Node

data class PinChallengeProps(val node: Node, val joinDetails: JoinDetails, val required: Boolean)
