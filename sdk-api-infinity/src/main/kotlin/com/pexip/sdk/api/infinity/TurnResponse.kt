package com.pexip.sdk.api.infinity

import kotlinx.serialization.Serializable

@Serializable
public data class TurnResponse(
    public val urls: List<String>,
    public val username: String,
    public val credential: String
)
