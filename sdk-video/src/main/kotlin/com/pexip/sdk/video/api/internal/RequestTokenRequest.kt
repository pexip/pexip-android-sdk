package com.pexip.sdk.video.api.internal

import kotlinx.serialization.Serializable

@Serializable
internal data class RequestTokenRequest(val display_name: String)
