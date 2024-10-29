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

import com.pexip.sdk.api.infinity.Token
import com.pexip.sdk.infinity.BreakoutId
import com.pexip.sdk.infinity.CallId
import com.pexip.sdk.infinity.ParticipantId
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.okio.decodeFromBufferedSource
import okhttp3.HttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import okhttp3.sse.EventSources

internal inline fun <reified T> Json.encodeToRequestBody(value: T) =
    encodeToString(value).toRequestBody(ApplicationJson)

internal fun <T> Json.encodeToRequestBody(serializer: SerializationStrategy<T>, value: T) =
    encodeToString(serializer, value).toRequestBody(ApplicationJson)

@OptIn(ExperimentalSerializationApi::class)
internal inline fun <reified T> Json.decodeFromResponseBody(
    deserializer: DeserializationStrategy<T>,
    body: ResponseBody,
) = decodeFromBufferedSource(deserializer, body.source())

internal inline fun EventSources.createFactory(
    client: OkHttpClient,
    block: OkHttpClient.Builder.() -> Unit,
) = createFactory(client.newBuilder().apply(block).build())

internal fun HttpUrl.Builder.addPathSegment(id: BreakoutId) = addPathSegment(id.value)

internal fun HttpUrl.Builder.addPathSegment(id: ParticipantId) = addPathSegment(id.value)

internal fun HttpUrl.Builder.addPathSegment(id: CallId) = addPathSegment(id.value)

internal fun HttpUrl.newApiClientV2Builder() = checkNotNull(newBuilder("/api/client/v2"))

internal fun HttpUrl.Builder.conference(conferenceAlias: String): HttpUrl.Builder = apply {
    require(conferenceAlias.isNotBlank()) { "conferenceAlias is blank." }
    addPathSegment("conferences")
    addPathSegment(conferenceAlias)
}

internal fun HttpUrl.Builder.breakout(id: BreakoutId) =
    addPathSegment("breakouts").addPathSegment(id)

internal fun HttpUrl.Builder.participant(id: ParticipantId) =
    addPathSegment("participants").addPathSegment(id)

internal fun HttpUrl.Builder.call(id: CallId) = addPathSegment("calls").addPathSegment(id)

internal fun HttpUrl.Builder.registration(deviceAlias: String? = null): HttpUrl.Builder = apply {
    deviceAlias?.let { require(it.isNotBlank()) { "deviceAlias is blank." } }
    addPathSegment("registrations")
    if (deviceAlias != null) addPathSegment(deviceAlias)
}

internal fun HttpUrl.Builder.token(token: Token): HttpUrl.Builder =
    addQueryParameter("token", token.token)

internal inline fun Request.Builder.url(
    url: HttpUrl,
    block: HttpUrl.Builder.() -> Unit,
): Request.Builder = url(url.newApiClientV2Builder().apply(block).build())

internal fun Request.Builder.token(token: Token): Request.Builder = header("token", token.token)

internal inline fun <reified T : Any> Request.Builder.withTag(tag: T?) = tag(T::class.java, tag)

internal inline fun <reified T : Any> Request.tagOrElse(block: () -> T) =
    tag(T::class.java) ?: block()

internal val EMPTY_REQUEST = "".toRequestBody()

private val ApplicationJson by lazy { "application/json; charset=utf-8".toMediaType() }
