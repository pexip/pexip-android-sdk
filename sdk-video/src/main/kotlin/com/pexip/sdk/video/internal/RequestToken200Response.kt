package com.pexip.sdk.video.internal

import kotlinx.serialization.Serializable
import kotlin.time.Duration

@Serializable
internal data class RequestToken200Response(
    val token: String,
    @Serializable(with = DurationSerializer::class)
    val expires: Duration,
    val participant_uuid: String,
)
