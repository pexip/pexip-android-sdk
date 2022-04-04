package com.pexip.sdk.api.infinity

import kotlinx.serialization.Serializable

@Serializable
@JvmInline
public value class UpdateResponse(public val sdp: String)
