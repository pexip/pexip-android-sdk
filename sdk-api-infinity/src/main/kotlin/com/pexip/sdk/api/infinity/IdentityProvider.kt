package com.pexip.sdk.api.infinity

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
