package com.pexip.sdk.api.infinity.internal

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class PreferredAspectRatioRequest(@SerialName("aspect_ratio") val aspectRatio: Float) {

    init {
        require(aspectRatio in 0f..2f) { "aspect ratio is not in the 0..2 range." }
    }
}
