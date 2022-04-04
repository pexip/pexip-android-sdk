package com.pexip.sdk.api.infinity

import kotlinx.serialization.Serializable

@Serializable
public data class NewCandidateRequest(
    public val candidate: String,
    public val mid: String,
    public val ufrag: String,
    public val pwd: String? = null,
)
