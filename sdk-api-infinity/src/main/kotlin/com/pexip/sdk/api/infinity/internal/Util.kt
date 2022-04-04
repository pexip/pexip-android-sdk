package com.pexip.sdk.api.infinity.internal

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.ResponseBody
import java.net.URL
import java.util.UUID

internal val UnitMapper = { _: Response -> }

internal inline fun <reified T> Json.encodeToRequestBody(value: T) =
    encodeToString(value).toRequestBody(ApplicationJson)

internal inline fun <reified T> Json.decodeFromResponseBody(
    deserializer: DeserializationStrategy<T>,
    body: ResponseBody,
) = decodeFromString(deserializer, body.string())

internal inline fun OkHttpClient.newCall(block: Request.Builder.() -> Unit) =
    newCall(Request.Builder().apply(block).build())

internal fun Request.Builder.url(node: URL, method: String): Request.Builder {
    require(method.isNotBlank()) { "method is blank." }
    return url(node) {
        client()
        addPathSegment(method)
    }
}

internal fun Request.Builder.url(
    node: URL,
    conferenceAlias: String,
    method: String,
): Request.Builder {
    require(method.isNotBlank()) { "method is blank." }
    return url(node) {
        client()
        conference(conferenceAlias)
        addPathSegment(method)
    }
}

internal fun Request.Builder.url(
    node: URL,
    conferenceAlias: String,
    participantId: UUID,
    method: String,
): Request.Builder {
    require(method.isNotBlank()) { "method is blank." }
    return url(node) {
        client()
        conference(conferenceAlias)
        participant(participantId)
        addPathSegment(method)
    }
}

internal fun Request.Builder.url(
    node: URL,
    conferenceAlias: String,
    participantId: UUID,
    callId: UUID,
    method: String,
): Request.Builder {
    require(method.isNotBlank()) { "method is blank." }
    return url(node) {
        client()
        conference(conferenceAlias)
        participant(participantId)
        call(callId)
        addPathSegment(method)
    }
}

private inline fun Request.Builder.url(url: URL, block: HttpUrl.Builder.() -> Unit) =
    url(url.toString().toHttpUrl().newBuilder().apply(block).build())

private fun HttpUrl.Builder.client() = addPathSegments("api/client/v2")

private fun HttpUrl.Builder.conference(conferenceAlias: String) =
    addPathSegments("conferences/$conferenceAlias")

private fun HttpUrl.Builder.participant(participantId: UUID) =
    addPathSegments("participants/$participantId")

private fun HttpUrl.Builder.call(callId: UUID) = addPathSegments("calls/$callId")

private val ApplicationJson by lazy { "application/json; charset=utf-8".toMediaType() }
