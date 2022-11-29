package com.pexip.sdk.api.infinity

import com.pexip.sdk.api.infinity.internal.FeccMovementSerializer
import kotlinx.serialization.Serializable

@Serializable(with = FeccMovementSerializer::class)
public enum class FeccMovement {
    PAN_LEFT, PAN_RIGHT, TILT_UP, TILT_DOWN, ZOOM_IN, ZOOM_OUT, UNKNOWN
}
