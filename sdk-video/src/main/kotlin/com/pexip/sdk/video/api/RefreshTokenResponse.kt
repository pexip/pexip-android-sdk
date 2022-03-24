package com.pexip.sdk.video.api

import kotlinx.serialization.Serializable

@Serializable
public data class RefreshTokenResponse(public val token: String)
