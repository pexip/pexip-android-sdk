package com.pexip.sdk.api.infinity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class CallsRequest(
    @SerialName("call_type")
    public val callType: String,
    public val sdp: String,
    public val present: String? = null,
)
