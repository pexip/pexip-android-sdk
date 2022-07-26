package com.pexip.sdk.api.infinity

import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.LongAsStringSerializer

@Serializable
public data class RefreshTokenResponse(
    override val token: String,
    @Serializable(with = LongAsStringSerializer::class)
    override val expires: Long,
) : Token
