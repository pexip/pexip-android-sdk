package com.pexip.sdk.video.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A SAML identity provider used for authentication.
 *
 * @property id a provider ID
 * @property name a display name of the provider
 */
@Serializable
public data class IdentityProvider(
    @SerialName("uuid")
    public val id: IdentityProviderId,
    public val name: String,
)
