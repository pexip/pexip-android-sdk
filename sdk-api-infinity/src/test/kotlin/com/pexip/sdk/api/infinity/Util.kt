package com.pexip.sdk.api.infinity

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import java.net.URL
import java.util.UUID
import kotlin.random.Random
import kotlin.random.nextInt
import kotlin.test.assertEquals

private const val CHARACTERS = "_-0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"

internal fun Random.nextString(length: Int) =
    CharArray(length) { CHARACTERS.random(this) }.concatToString()

internal fun Random.nextIdentityProviderId() = IdentityProviderId(nextUuid())

internal fun Random.nextPin(): String = "${nextInt(1000..9999)}"

internal fun Random.nextSsoToken() = nextString(16)

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

internal fun RecordedRequest.assertRequestUrl(url: URL, block: HttpUrl.Builder.() -> Unit) =
    assertEquals(url.toString().toHttpUrl().newBuilder().apply(block).build(), requestUrl)

internal fun RecordedRequest.assertToken(token: String?) = assertEquals(token, getHeader("token"))

internal fun RecordedRequest.assertPin(pin: String?) = assertEquals(pin, getHeader("pin"))

private fun RecordedRequest.assertContentType(contentType: String?) =
    assertEquals(contentType, getHeader("Content-Type"))

private fun Random.nextUuid() = UUID.randomUUID().toString()
