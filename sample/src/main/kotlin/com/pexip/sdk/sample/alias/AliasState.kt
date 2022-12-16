package com.pexip.sdk.sample.alias

import com.pexip.sdk.sample.pinrequirement.PinRequirementProps

data class AliasState(
    val conferenceAlias: String = "",
    val host: String = "",
    val presentationInMain: Boolean = false,
    val pinRequirementProps: PinRequirementProps? = null,
)
