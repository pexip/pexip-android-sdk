package com.pexip.sdk.sample.pinrequirement

import java.net.URL

sealed class PinRequirementState {

    object ResolvingNode : PinRequirementState()

    data class ResolvingPinRequirement(val node: URL) : PinRequirementState()

    data class Failure(val t: Throwable) : PinRequirementState()
}
