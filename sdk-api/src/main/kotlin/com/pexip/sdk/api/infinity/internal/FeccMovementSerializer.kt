/*
 * Copyright 2022-2023 Pexip AS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.pexip.sdk.api.infinity.internal

import com.pexip.sdk.api.infinity.FeccMovement
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure

internal object FeccMovementSerializer : KSerializer<FeccMovement> {

    private const val AXIS_PAN = "pan"
    private const val AXIS_TILT = "tilt"
    private const val AXIS_ZOOM = "zoom"
    private const val DIRECTION_LEFT = "left"
    private const val DIRECTION_RIGHT = "right"
    private const val DIRECTION_UP = "up"
    private const val DIRECTION_DOWN = "down"
    private const val DIRECTION_IN = "in"
    private const val DIRECTION_OUT = "out"

    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("FeccMovement") {
        element<String>("axis")
        element<String>("direction")
    }

    override fun serialize(encoder: Encoder, value: FeccMovement) {
        if (value == FeccMovement.UNKNOWN) return
        encoder.encodeStructure(descriptor) {
            encodeStringElement(descriptor, 0, value.axis)
            encodeStringElement(descriptor, 1, value.direction)
        }
    }

    override fun deserialize(decoder: Decoder): FeccMovement = decoder.decodeStructure(descriptor) {
        var axis: String? = null
        var direction: String? = null
        while (true) {
            when (val index = decodeElementIndex(descriptor)) {
                CompositeDecoder.DECODE_DONE -> break
                0 -> axis = decodeStringElement(descriptor, 0)
                1 -> direction = decodeStringElement(descriptor, 1)
                else -> error("Unexpected index: $index")
            }
        }
        FeccMovement(axis, direction)
    }

    private val FeccMovement.axis
        get() = when (this) {
            FeccMovement.PAN_LEFT, FeccMovement.PAN_RIGHT -> AXIS_PAN
            FeccMovement.TILT_UP, FeccMovement.TILT_DOWN -> AXIS_TILT
            FeccMovement.ZOOM_IN, FeccMovement.ZOOM_OUT -> AXIS_ZOOM
            FeccMovement.UNKNOWN -> error("Invalid value: $this.")
        }

    private val FeccMovement.direction
        get() = when (this) {
            FeccMovement.PAN_LEFT -> DIRECTION_LEFT
            FeccMovement.PAN_RIGHT -> DIRECTION_RIGHT
            FeccMovement.TILT_UP -> DIRECTION_UP
            FeccMovement.TILT_DOWN -> DIRECTION_DOWN
            FeccMovement.ZOOM_IN -> DIRECTION_IN
            FeccMovement.ZOOM_OUT -> DIRECTION_OUT
            FeccMovement.UNKNOWN -> error("Invalid value: $this.")
        }

    @Suppress("ktlint:standard:function-naming")
    private fun FeccMovement(axis: String?, direction: String?) = when (axis) {
        AXIS_PAN -> when (direction) {
            DIRECTION_LEFT -> FeccMovement.PAN_LEFT
            DIRECTION_RIGHT -> FeccMovement.PAN_RIGHT
            else -> FeccMovement.UNKNOWN
        }
        AXIS_TILT -> when (direction) {
            DIRECTION_UP -> FeccMovement.TILT_UP
            DIRECTION_DOWN -> FeccMovement.TILT_DOWN
            else -> FeccMovement.UNKNOWN
        }
        AXIS_ZOOM -> when (direction) {
            DIRECTION_IN -> FeccMovement.ZOOM_IN
            DIRECTION_OUT -> FeccMovement.ZOOM_OUT
            else -> FeccMovement.UNKNOWN
        }
        else -> FeccMovement.UNKNOWN
    }
}
