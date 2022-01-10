package com.pexip.sdk.video.api.internal

import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.LongAsStringSerializer

@Serializable
internal class RequestToken200Response(
    val token: String,
    @Serializable(with = LongAsStringSerializer::class)
    val expires: Long,
) : PinRequirementResponse
