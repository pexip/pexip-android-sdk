package com.pexip.sdk.video.internal

import com.pexip.sdk.video.IdentityProvider
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

@Serializable
internal class RequiredPinResponse(val guest_pin: String)

@Serializable
internal class RequiredSsoResponse(val idp: List<IdentityProvider>)

@Serializable
internal class SsoRedirectResponse(val redirect_url: String, val redirect_idp: IdentityProvider)

/**
 * Since the REST API doesn't contain any "type" field to determine which response is it, use
 * available fields to guess.
 */
internal object RequestToken403Serializer : JsonContentPolymorphicSerializer<Any>(Any::class) {

    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<out Any> {
        if (element is JsonPrimitive) {
            if (element.isString) return String.serializer()
        } else if (element is JsonObject) {
            if ("redirect_url" in element) return SsoRedirectResponse.serializer()
            if ("guest_pin" in element) return RequiredPinResponse.serializer()
            if ("idp" in element) return RequiredSsoResponse.serializer()
        }
        throw SerializationException("Failed to deserialize body.")
    }
}
