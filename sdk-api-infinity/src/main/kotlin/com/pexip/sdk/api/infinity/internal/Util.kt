package com.pexip.sdk.api.infinity.internal

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.okio.decodeFromBufferedSource
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import okhttp3.sse.EventSources
import java.net.URL
import java.util.UUID

internal inline fun <reified T> Json.encodeToRequestBody(value: T) =
    encodeToString(value).toRequestBody(ApplicationJson)

@OptIn(ExperimentalSerializationApi::class)
internal inline fun <reified T> Json.decodeFromResponseBody(
    deserializer: DeserializationStrategy<T>,
    body: ResponseBody,
) = decodeFromBufferedSource(deserializer, body.source())

internal inline fun EventSources.createFactory(
    client: OkHttpClient,
    block: OkHttpClient.Builder.() -> Unit,
) = createFactory(client.newBuilder().apply(block).build())

internal fun HttpUrl.Builder.addPathSegment(uuid: UUID) = addPathSegment(uuid.toString())

internal fun HttpUrl.Builder.conference(conferenceAlias: String): HttpUrl.Builder = apply {
    require(conferenceAlias.isNotBlank()) { "conferenceAlias is blank." }
    addPathSegment("conferences")
    addPathSegment(conferenceAlias)
}

internal fun HttpUrl.Builder.participant(participantId: UUID) =
    addPathSegment("participants").addPathSegment(participantId)

internal fun HttpUrl.Builder.call(callId: UUID) =
    addPathSegment("calls").addPathSegment(callId)

internal fun HttpUrl.Builder.registration(deviceAlias: String? = null): HttpUrl.Builder = apply {
    deviceAlias?.let { require(it.isNotBlank()) { "deviceAlias is blank." } }
    addPathSegment("registrations")
    if (deviceAlias != null) addPathSegment(deviceAlias)
}

internal inline fun Request.Builder.url(
    url: URL,
    block: HttpUrl.Builder.() -> Unit,
): Request.Builder {
    val httpUrl = checkNotNull(url.toHttpUrlOrNull())
    val builder = checkNotNull(httpUrl.newBuilder("/api/client/v2"))
    return url(builder.apply(block).build())
}

private val ApplicationJson by lazy { "application/json; charset=utf-8".toMediaType() }
