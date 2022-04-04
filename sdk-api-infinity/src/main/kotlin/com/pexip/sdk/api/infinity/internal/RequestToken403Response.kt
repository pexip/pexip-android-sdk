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

    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<out RequestToken403Response> {
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
