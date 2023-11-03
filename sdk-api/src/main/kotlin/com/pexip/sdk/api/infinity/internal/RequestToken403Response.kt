/*
 * Copyright 2022-2024 Pexip AS
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

import com.pexip.sdk.api.infinity.IdentityProvider
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

internal sealed interface RequestToken403Response

@Serializable
@JvmInline
internal value class ErrorResponse(val message: String) : RequestToken403Response

@Serializable
internal data class RequiredPinResponse(val guest_pin: String) : RequestToken403Response

@Serializable
internal data class RequiredSsoResponse(val idp: List<IdentityProvider>) : RequestToken403Response

@Serializable
internal data class SsoRedirectResponse(
    val redirect_url: String,
    val redirect_idp: IdentityProvider,
) : RequestToken403Response

internal object RequestToken403ResponseSerializer :
    UnboxingSerializer<RequestToken403Response>(PolymorphicRequestToken403ResponseSerializer)

/**
 * Since the REST API doesn't contain any "type" field to determine which response is it, use
 * available fields to guess.
 */
private object PolymorphicRequestToken403ResponseSerializer :
    JsonContentPolymorphicSerializer<RequestToken403Response>(RequestToken403Response::class) {

    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<RequestToken403Response> {
        if (element is JsonPrimitive) {
            if (element.isString) return ErrorResponse.serializer()
        } else if (element is JsonObject) {
            if ("redirect_url" in element) return SsoRedirectResponse.serializer()
            if ("guest_pin" in element) return RequiredPinResponse.serializer()
            if ("idp" in element) return RequiredSsoResponse.serializer()
        }
        throw SerializationException("Failed to deserialize body.")
    }
}
