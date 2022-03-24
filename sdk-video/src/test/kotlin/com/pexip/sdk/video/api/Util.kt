package com.pexip.sdk.video.api

import com.pexip.sdk.video.nextString
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import okio.Buffer
import java.util.UUID
import kotlin.random.Random
import kotlin.random.nextInt

internal fun Random.nextConferenceAlias() = ConferenceAlias(nextString(8))

internal fun Random.nextCallId() = CallId(nextUuid())

internal fun Random.nextParticipantId() = ParticipantId(nextUuid())

internal fun Random.nextIdentityProviderId() = IdentityProviderId(nextUuid())

internal fun Random.nextPin(): String = "${nextInt(1000..9999)}"

internal fun Random.nextSsoToken() = nextString(16)

internal inline fun MockWebServer.enqueue(block: MockResponse.() -> Unit) =
    enqueue(MockResponse().apply(block))

internal inline fun MockWebServer.takeRequest(block: RecordedRequest.() -> Unit) =
    with(takeRequest(), block)

@OptIn(ExperimentalSerializationApi::class)
internal inline fun <reified T> Json.decodeFromBuffer(buffer: Buffer) =
    decodeFromStream<T>(buffer.inputStream())

private fun Random.nextUuid() = UUID.randomUUID().toString()
