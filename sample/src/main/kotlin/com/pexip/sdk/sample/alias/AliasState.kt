package com.pexip.sdk.sample.alias

import android.os.Parcelable
import com.pexip.sdk.sample.pinrequirement.PinRequirementProps
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
data class AliasState(
    val conferenceAlias: String = "",
    val host: String = "",
    val presentationInMain: Boolean = false,
    @IgnoredOnParcel val pinRequirementProps: PinRequirementProps? = null,
) : Parcelable
