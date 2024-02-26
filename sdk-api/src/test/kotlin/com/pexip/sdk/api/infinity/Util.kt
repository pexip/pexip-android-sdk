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
package com.pexip.sdk.api.infinity

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import okio.BufferedSource
import okio.ByteString.Companion.encodeUtf8
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import java.net.URL
import java.util.UUID
import kotlin.random.Random
import kotlin.random.nextInt
import kotlin.test.assertEquals
import kotlin.time.DurationUnit
import kotlin.time.toDuration

private const val CHARACTERS = "_-0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"

internal fun FileSystem.readUtf8(path: String): String = readUtf8(path.toPath())

internal fun FileSystem.readUtf8(path: Path) = read(path, BufferedSource::readUtf8)

internal fun Random.nextString(length: Int) =
    CharArray(length) { CHARACTERS.random(this) }.concatToString()

internal fun Random.nextDigits(length: Int) =
    CharArray(length) { DtmfRequest.ALLOWED_DIGITS.random(this) }.concatToString()

internal fun Random.nextDuration(unit: DurationUnit = DurationUnit.SECONDS) =
    nextInt(0, 1000).toDuration(unit)

internal fun Random.nextIdentityProviderId() = IdentityProviderId(nextUuid())

internal fun Random.nextPin(): String = "${nextInt(1000..9999)}"

internal fun Random.nextMessageRequest() = MessageRequest(
    payload = nextString(8),
    type = nextString(8),
)

internal inline fun MockWebServer.enqueue(block: MockResponse.() -> Unit) =
    enqueue(MockResponse().apply(block))

internal inline fun MockWebServer.takeRequest(block: RecordedRequest.() -> Unit) =
    with(takeRequest(), block)

internal fun RecordedRequest.assertGet() = assertEquals("GET", method)

internal fun RecordedRequest.assertPostEmptyBody() {
    assertEquals("POST", method)
    assertContentType(null)
    assertEquals(0, bodySize)
}

internal inline fun <reified T> RecordedRequest.assertPost(json: Json, request: T) {
    assertEquals("POST", method)
    assertContentType("application/json; charset=utf-8")
    assertEquals(request, json.decodeFromString(body.readUtf8()))
}

internal fun <T> RecordedRequest.assertPost(
    json: Json,
    serializer: DeserializationStrategy<T>,
    request: T,
) {
    assertEquals("POST", method)
    assertContentType("application/json; charset=utf-8")
    assertEquals(request, json.decodeFromString(serializer, body.readUtf8()))
}

internal fun RecordedRequest.assertRequestUrl(url: URL, block: HttpUrl.Builder.() -> Unit) =
    assertEquals(url.toString().toHttpUrl().newBuilder().apply(block).build(), requestUrl)

internal fun RecordedRequest.assertRequestUrl(url: HttpUrl, block: HttpUrl.Builder.() -> Unit) =
    assertEquals(url.newBuilder().apply(block).build(), requestUrl)

internal fun RecordedRequest.assertToken(token: String?) = assertEquals(token, getHeader("token"))

internal fun RecordedRequest.assertAuthorization(username: String, password: String) {
    val base64 = "$username:$password".encodeUtf8().base64Url()
    assertEquals("x-pexip-basic $base64", getHeader("Authorization"))
}

internal fun RecordedRequest.assertPin(pin: String?) = assertEquals(
    expected = pin?.let { if (it.isBlank()) "none" else it.trim() },
    actual = getHeader("pin"),
)

private fun RecordedRequest.assertContentType(contentType: String?) =
    assertEquals(contentType, getHeader("Content-Type"))

private fun Random.nextUuid() = UUID.randomUUID().toString()
