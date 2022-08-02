package com.pexip.sdk.api.infinity

import com.pexip.sdk.api.infinity.internal.UUIDSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.LongAsStringSerializer
import java.util.UUID

@Serializable
public data class RequestTokenResponse(
    override val token: String,
    @Serializable(with = LongAsStringSerializer::class)
    override val expires: Long,
    @Serializable(with = UUIDSerializer::class)
    @SerialName("participant_uuid")
    public val participantId: UUID,
    @SerialName("display_name")
    public val participantName: String,
    public val version: VersionResponse,
    @SerialName("analytics_enabled")
    public val analyticsEnabled: Boolean = false,
    public val stun: List<StunResponse> = emptyList(),
    public val turn: List<TurnResponse> = emptyList(),
) : Token
