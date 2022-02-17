package com.pexip.sdk.video

import kotlinx.serialization.Serializable

@Serializable
internal data class Box<T : Any>(val result: T)
