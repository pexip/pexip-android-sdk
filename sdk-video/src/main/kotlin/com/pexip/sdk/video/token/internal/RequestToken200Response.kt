package com.pexip.sdk.video.token.internal

import com.pexip.sdk.video.internal.UnboxingSerializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@Serializable
internal data class RequestToken200Response(
    val token: String,
    @Serializable(with = DurationSerializer::class)
    val expires: Duration,
    val participant_uuid: String,
)

internal object RequestToken200ResponseSerializer :
    UnboxingSerializer<RequestToken200Response>(RequestToken200Response.serializer())

private object DurationSerializer : KSerializer<Duration> {

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(
        serialName = "Duration",
        kind = PrimitiveKind.STRING
    )

    override fun serialize(encoder: Encoder, value: Duration) =
        encoder.encodeString(value.inWholeSeconds.toString())

    override fun deserialize(decoder: Decoder): Duration = decoder.decodeString().toLong().seconds
}
