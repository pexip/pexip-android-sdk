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

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import assertk.assertions.isZero
import com.pexip.sdk.infinity.Node
import com.pexip.sdk.infinity.test.nextString
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import okio.BufferedSource
import okio.ByteString.Companion.encodeUtf8
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import kotlin.random.Random
import kotlin.random.nextInt
import kotlin.time.DurationUnit
import kotlin.time.toDuration

internal fun String.quoted() = "\"$this\""

internal fun FileSystem.readUtf8(path: String): String = readUtf8(path.toPath())

internal fun FileSystem.readUtf8(path: Path) = read(path, BufferedSource::readUtf8)

internal fun Random.nextDigits(length: Int = 8) =
    CharArray(length) { DtmfRequest.ALLOWED_DIGITS.random(this) }.concatToString()

internal fun Random.nextDuration(unit: DurationUnit = DurationUnit.SECONDS) =
    nextInt(0, 1000).toDuration(unit)

internal fun Random.nextIdentityProviderId() = IdentityProviderId(nextString())

internal fun Random.nextPin(): String = "${nextInt(1000..9999)}"

internal fun Random.nextMessageRequest() = MessageRequest(
    payload = nextString(),
    type = nextString(),
)

internal fun InfinityService.newRequest(url: HttpUrl) = newRequest(Node(url.host, url.port))

internal inline fun MockWebServer.enqueue(block: MockResponse.() -> Unit) =
    enqueue(MockResponse().apply(block))

internal inline fun MockWebServer.takeRequest(block: RecordedRequest.() -> Unit) =
    with(takeRequest(), block)

internal fun RecordedRequest.assertGet() = assertThatMethod().isEqualTo("GET")

internal fun RecordedRequest.assertPostEmptyBody() {
    assertThatMethod().isEqualTo("POST")
    assertThatHeader("Content-Type").isNull()
    assertThat(::bodySize).isZero()
}

internal inline fun <reified T> RecordedRequest.assertPost(json: Json, request: T) {
    assertThatMethod().isEqualTo("POST")
    assertThatHeader("Content-Type").isEqualTo("application/json; charset=utf-8")
    assertThat(json.decodeFromString<T>(body.readUtf8()), "request").isEqualTo(request)
}

internal fun <T> RecordedRequest.assertPost(
    json: Json,
    serializer: DeserializationStrategy<T>,
    request: T,
) {
    assertThatMethod().isEqualTo("POST")
    assertThatHeader("Content-Type").isEqualTo("application/json; charset=utf-8")
    assertThat(json.decodeFromString(serializer, body.readUtf8()), "request").isEqualTo(request)
}

internal fun RecordedRequest.assertRequestUrl(url: HttpUrl, block: HttpUrl.Builder.() -> Unit) =
    assertThat(::requestUrl).isEqualTo(url.newBuilder().apply(block).build())

internal fun RecordedRequest.assertToken(token: Token?) =
    assertThatHeader("token").isEqualTo(token?.token)

internal fun RecordedRequest.assertAuthorization(username: String, password: String) {
    val base64 = "$username:$password".encodeUtf8().base64Url()
    assertThatHeader("Authorization").isEqualTo("x-pexip-basic $base64")
}

internal fun RecordedRequest.assertPin(pin: String?) =
    assertThatHeader("pin").isEqualTo(pin?.let { if (it.isBlank()) "none" else it.trim() })

private fun RecordedRequest.assertThatMethod() = assertThat(::method)

private fun RecordedRequest.assertThatHeader(name: String) = assertThat(getHeader(name), name)
