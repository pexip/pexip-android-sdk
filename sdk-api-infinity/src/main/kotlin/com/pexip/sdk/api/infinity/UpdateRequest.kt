package com.pexip.sdk.api.infinity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class UpdateRequest(
    val sdp: String,
    @SerialName("fecc_supported")
    public val fecc: Boolean = false,
)
