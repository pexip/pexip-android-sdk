package com.pexip.sdk.video.token.internal

import kotlinx.serialization.Serializable

@Serializable
internal data class RequestTokenRequest(
    val display_name: String,
    val conference_extension: String,
    val chosen_idp: String? = null,
    val sso_token: String? = null,
)
