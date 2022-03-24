package com.pexip.sdk.video.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class CallsResponse(
    @SerialName("call_uuid")
    public val callId: CallId,
    public val sdp: String,
)
