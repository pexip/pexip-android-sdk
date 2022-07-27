package com.pexip.sdk.sample.pinrequirement

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.net.URL

sealed class PinRequirementState : Parcelable {

    @Parcelize
    object ResolvingNode : PinRequirementState()

    @Parcelize
    data class ResolvingPinRequirement(val node: URL) : PinRequirementState()

    @Parcelize
    data class Failure(val t: Throwable) : PinRequirementState()
}
