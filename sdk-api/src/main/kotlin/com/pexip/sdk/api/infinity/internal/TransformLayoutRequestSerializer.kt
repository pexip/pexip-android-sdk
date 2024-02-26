/*
 * Copyright 2024 Pexip AS
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

import com.pexip.sdk.api.infinity.TransformLayoutRequest
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonTransformingSerializer
import kotlinx.serialization.json.buildJsonObject

internal object TransformLayoutRequestSerializer :
    JsonTransformingSerializer<TransformLayoutRequest>(TransformLayoutRequest.serializer()) {

    private const val KEY_TRANSFORMS = "transforms"

    override fun transformSerialize(element: JsonElement): JsonElement = when (element) {
        is JsonObject -> buildJsonObject { put(KEY_TRANSFORMS, element) }
        else -> element
    }

    override fun transformDeserialize(element: JsonElement): JsonElement = when (element) {
        is JsonObject -> element.getValue(KEY_TRANSFORMS)
        else -> element
    }
}
