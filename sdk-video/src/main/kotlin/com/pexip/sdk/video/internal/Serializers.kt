package com.pexip.sdk.video.internal

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.JsonTransformingSerializer
import kotlinx.serialization.json.jsonObject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

internal object RequestToken200ResponseSerializer :
    UnboxingSerializer<RequestToken200Response>(RequestToken200Response.serializer())

internal object RequestToken403ResponseSerializer :
    UnboxingSerializer<RequestToken403Response>(PolymorphicRequestToken403ResponseSerializer)

internal object RefreshToken200ResponseSerializer :
    UnboxingSerializer<RefreshToken200Response>(RefreshToken200Response.serializer())

internal object CallsResponseSerializer :
    UnboxingSerializer<CallsResponse>(CallsResponse.serializer())

internal object StringSerializer : UnboxingSerializer<String>(String.serializer())

internal object DurationSerializer : KSerializer<Duration> {

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(
        serialName = "Duration",
        kind = PrimitiveKind.STRING
    )

    override fun serialize(encoder: Encoder, value: Duration) =
        encoder.encodeString(value.inWholeSeconds.toString())

    override fun deserialize(decoder: Decoder): Duration = decoder.decodeString().toLong().seconds
}

internal abstract class UnboxingSerializer<T : Any>(tSerializer: KSerializer<T>) :
    JsonTransformingSerializer<T>(tSerializer) {

    final override fun transformDeserialize(element: JsonElement): JsonElement =
        element.jsonObject.getValue("result")
}

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
