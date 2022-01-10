package com.pexip.sdk.sample.pinrequirement

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed class PinRequirementState : Parcelable {

    @Parcelize
    object ResolvingPinRequirement : PinRequirementState()

    @Parcelize
    data class Failure(val t: Throwable) : PinRequirementState()
}
