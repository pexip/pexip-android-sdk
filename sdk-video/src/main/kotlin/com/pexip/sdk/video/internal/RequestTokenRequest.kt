package com.pexip.sdk.video.internal

import kotlinx.serialization.Serializable

@Serializable
internal data class RequestTokenRequest(
    val display_name: String,
    val conference_extension: String,
)
