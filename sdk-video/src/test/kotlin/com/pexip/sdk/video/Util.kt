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

internal fun Random.nextAlias(): String = "${nextInt(100000..999999)}"

internal fun Random.nextPin(): String = "${nextInt(1000..9999)}"

internal fun Random.nextToken() = "${nextInt(100000000..999999999)}"

@OptIn(ExperimentalSerializationApi::class)
internal inline fun <reified T> Json.decodeFromBuffer(buffer: Buffer) =
    decodeFromStream<T>(buffer.inputStream())

internal inline fun MockWebServer.enqueue(block: MockResponse.() -> Unit) =
    enqueue(MockResponse().apply(block))

internal inline fun MockWebServer.takeRequest(block: RecordedRequest.() -> Unit) =
    with(takeRequest(), block)
