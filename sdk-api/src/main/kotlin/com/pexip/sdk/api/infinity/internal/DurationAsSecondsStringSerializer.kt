/*
 * Copyright 2023-2024 Pexip AS
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

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.time.Duration

internal object DurationAsSecondsStringSerializer : KSerializer<Duration> {

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("DurationAsSecondsStringSerializer", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Duration =
        Duration.parseIsoString("PT${decoder.decodeString()}S")

    override fun serialize(encoder: Encoder, value: Duration) {
        val s = value.toComponents { seconds, nanoseconds ->
            buildString {
                append(seconds)
                if (nanoseconds != 0) {
                    append('.')
                    append(nanoseconds)
                }
            }
        }
        encoder.encodeString(s)
    }
}
