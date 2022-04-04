package com.pexip.sdk.api.infinity

import kotlinx.serialization.Serializable

@Serializable
internal data class Box<T : Any>(val result: T)
