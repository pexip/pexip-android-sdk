package com.pexip.sdk.api.infinity

import kotlinx.serialization.Serializable

@Serializable
public data class RegistrationResponse(
    val alias: String,
    val description: String = "",
    val username: String = "",
)
