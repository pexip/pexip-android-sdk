package com.pexip.sdk.video

import kotlinx.serialization.Serializable

/**
 * A SAML identity provider used for authentication.
 *
 * @property name a display name of the provider
 * @property uuid a provider ID
 */
@Serializable
public class IdentityProvider(public val name: String, public val uuid: String) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is IdentityProvider) return false
        if (name != other.name) return false
        if (uuid != other.uuid) return false
        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + uuid.hashCode()
        return result
    }

    override fun toString(): String = "IdentityProvider(name=$name, uuid=$uuid)"
}
