package com.pexip.sdk.video.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class RequestTokenRequest(
    @SerialName("conference_extension")
    public val conferenceExtension: String? = null,
    @SerialName("display_name")
    public val displayName: String? = null,
    @SerialName("chosen_idp")
    public val chosenIdp: IdentityProviderId? = null,
    @SerialName("sso_token")
    public val ssoToken: String? = null,
)
