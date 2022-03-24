package com.pexip.sdk.video.api.internal

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody

internal inline fun <reified T> Json.encodeToRequestBody(value: T) =
    encodeToString(value).toRequestBody(ApplicationJson)

internal inline fun <reified T> Json.decodeFromResponseBody(
    deserializer: DeserializationStrategy<T>,
    body: ResponseBody,
) = decodeFromString(deserializer, body.string())

internal inline fun OkHttpClient.newCall(block: Request.Builder.() -> Unit) =
    newCall(Request.Builder().apply(block).build())

private val ApplicationJson by lazy { "application/json; charset=utf-8".toMediaType() }
