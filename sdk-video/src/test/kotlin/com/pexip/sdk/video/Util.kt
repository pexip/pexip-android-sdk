package com.pexip.sdk.video

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import okio.Buffer
import kotlin.random.Random
import kotlin.random.nextInt

private const val CHARACTERS = "_-0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"

internal fun Random.nextString(length: Int) =
    CharArray(length) { CHARACTERS.random(this) }.concatToString()

internal fun Random.nextPin(): String = "${nextInt(1000..9999)}"

internal fun Random.nextToken() = nextString(16)

internal fun Random.nextSsoToken() = nextString(16)

@OptIn(ExperimentalSerializationApi::class)
internal inline fun <reified T> Json.decodeFromBuffer(buffer: Buffer) =
    decodeFromStream<T>(buffer.inputStream())

internal inline fun MockWebServer.enqueue(block: MockResponse.() -> Unit) =
    enqueue(MockResponse().apply(block))

internal inline fun MockWebServer.takeRequest(block: RecordedRequest.() -> Unit) =
    with(takeRequest(), block)
