/*
 * Copyright 2023 Pexip AS
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
package com.pexip.sdk.api.infinity

import com.pexip.sdk.api.infinity.internal.UUIDSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import java.util.UUID

@Serializable
public sealed interface DataChannelMessage {

    @Serializable
    @SerialName("message")
    public data class Message(val body: Body) : DataChannelMessage {

        @Serializable
        public data class Body(
            val type: String,
            val payload: String,
            @Serializable(with = UUIDSerializer::class)
            @SerialName("uuid")
            val senderId: UUID,
            @SerialName("origin")
            val senderName: String = "",
        )
    }

    @Serializable
    public data object Unknown : DataChannelMessage {

        override fun encodeToString(): String =
            throw IllegalArgumentException("Unknown doesn't have a serialized form.")
    }

    /**
     * Serializes this instance to a JSON string.
     */
    public fun encodeToString(): String = InfinityService.Json.encodeToString(this)

    public companion object {

        /**
         * Decodes the given [string] to an instance of [DataChannelMessage].
         *
         * @throws SerializationException in case of any decoding-specific error
         */
        public fun decodeFromString(string: String): DataChannelMessage =
            InfinityService.Json.decodeFromString(string)
    }
}
