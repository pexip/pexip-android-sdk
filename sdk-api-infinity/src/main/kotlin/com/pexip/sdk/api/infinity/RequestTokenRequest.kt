package com.pexip.sdk.api.infinity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

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
    @Transient
    public val incomingToken: String? = null,
)
