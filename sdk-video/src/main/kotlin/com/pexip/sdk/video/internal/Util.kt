package com.pexip.sdk.video.internal

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import java.util.concurrent.ExecutorService

internal fun ExecutorService.maybeSubmit(task: Runnable) = when (isShutdown) {
    true -> null
    else -> submit(task)
}

internal inline fun <reified T> Json.encodeToRequestBody(value: T) =
    encodeToString(value).toRequestBody(ApplicationJson)

internal inline fun <reified T> Json.decodeFromResponseBody(
    deserializer: DeserializationStrategy<T>,
    body: ResponseBody,
) = decodeFromString(deserializer, body.string())

internal inline fun HttpUrl(url: HttpUrl, block: HttpUrl.Builder.() -> Unit) =
    url.newBuilder().apply(block).build()

internal inline fun Request.Builder.url(url: HttpUrl, block: HttpUrl.Builder.() -> Unit) =
    url(HttpUrl(url, block))

internal inline fun OkHttpClient.newCall(block: Request.Builder.() -> Unit) =
    newCall(Request(block))

internal inline fun OkHttpClient.execute(block: Request.Builder.() -> Unit) =
    newCall(block).execute()

internal inline fun Request(block: Request.Builder.() -> Unit) =
    Request.Builder().apply(block).build()

private val ApplicationJson by lazy { "application/json; charset=utf-8".toMediaType() }
