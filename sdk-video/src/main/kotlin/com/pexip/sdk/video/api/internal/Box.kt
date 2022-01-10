package com.pexip.sdk.video.api.internal

import kotlinx.serialization.Serializable

@Serializable
internal data class Box<T : Any>(val result: T)
