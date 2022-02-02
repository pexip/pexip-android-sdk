package com.pexip.sdk.video.sample.pinrequirement

sealed class PinRequirementRendering {

    object ResolvingPinRequirement : PinRequirementRendering() {

        override fun toString(): String = "ResolvingPinRequirement"
    }

    data class Failure(val t: Throwable, val onBackClick: () -> Unit) : PinRequirementRendering()
}
