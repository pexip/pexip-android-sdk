package com.pexip.sdk.api.infinity

import com.pexip.sdk.api.infinity.internal.UUIDSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.LongAsStringSerializer
import java.util.UUID

@Serializable
public data class RequestTokenResponse(
    public val token: String,
    @Serializable(with = LongAsStringSerializer::class)
    public val expires: Long,
    @Serializable(with = UUIDSerializer::class)
    @SerialName("participant_uuid")
    public val participantId: UUID,
    @SerialName("display_name")
    public val participantName: String,
)
