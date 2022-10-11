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

internal inline fun HttpUrl(url: URL, block: HttpUrl.Builder.() -> Unit) =
    HttpUrl(checkNotNull(url.toHttpUrlOrNull()), block)

internal inline fun HttpUrl(url: HttpUrl, block: HttpUrl.Builder.() -> Unit) =
    url.newBuilder().apply(block).build()

internal fun HttpUrl.Builder.addPathSegment(uuid: UUID) = addPathSegment(uuid.toString())

private val ApplicationJson by lazy { "application/json; charset=utf-8".toMediaType() }
