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

import com.pexip.sdk.api.infinity.ParticipantResponse
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.JsonTransformingSerializer
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

internal object ParticipantResponseSerializer :
    JsonTransformingSerializer<ParticipantResponse>(ParticipantResponse.serializer()) {

    private val FloatTimestampKeys = setOf("start_time", "buzz_time", "spotlight")
    private val YesNoKeys = setOf(
        "is_muted",
        "is_presenting",
        "is_video_call",
        "is_audio_only_call",
        "presentation_supported",
        "fecc_supported",
        "transfer_supported",
        "disconnect_supported",
        "mute_supported",
    )

    override fun transformDeserialize(element: JsonElement): JsonElement = buildJsonObject {
        for ((key, value) in element.jsonObject) when (key) {
            in FloatTimestampKeys -> put(key, value.toInstantComponents())
            in YesNoKeys, "encryption" -> put(key, value.toBoolean())
            "role" -> put(key, value.toRole())
            else -> put(key, value)
        }
    }

    private fun JsonElement.toInstantComponents(): JsonElement {
        val content = jsonPrimitive.content
        if (content == "0") return JsonNull
        val parts = content.split(".", limit = 2)
        return buildJsonObject {
            val epochSeconds = parts[0]
            put("epochSeconds", epochSeconds)
            val nanosecondsOfSecond = parts.getOrNull(1)
                ?.padEnd(9, '0')
                ?.trimStart('0')
                ?.takeIf(String::isNotEmpty)
                ?: return@buildJsonObject
            put("nanosecondsOfSecond", nanosecondsOfSecond)
        }
    }

    private fun JsonElement.toBoolean() = when (jsonPrimitive.content) {
        "YES", "On" -> JsonPrimitive(true)
        "NO", "Off" -> JsonPrimitive(false)
        else -> JsonNull
    }

    private fun JsonElement.toRole() = when (jsonPrimitive.content) {
        "chair" -> JsonPrimitive("HOST")
        "guest" -> JsonPrimitive("GUEST")
        else -> JsonNull
    }
}
