package com.pexip.sdk.api.infinity

import com.pexip.sdk.api.infinity.internal.UUIDSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
public data class CallsResponse(
    @Serializable(with = UUIDSerializer::class)
    @SerialName("call_uuid")
    public val callId: UUID,
    public val sdp: String,
)
