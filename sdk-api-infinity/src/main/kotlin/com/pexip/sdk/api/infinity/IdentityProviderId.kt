package com.pexip.sdk.api.infinity

import kotlinx.serialization.Serializable

@Serializable
@JvmInline
public value class IdentityProviderId(internal val value: String) {

    init {
        require(value.isNotBlank()) { "value is blank." }
    }
}
