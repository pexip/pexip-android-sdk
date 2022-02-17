package com.pexip.sdk.video.internal

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.ResponseBody
import java.io.IOException
import kotlin.coroutines.resumeWithException

internal inline fun <reified T> Json.encodeToRequestBody(value: T) =
    encodeToString(value).toRequestBody(ApplicationJson)

internal inline fun <reified T> Json.decodeFromResponseBody(
    deserializer: DeserializationStrategy<T>,
    body: ResponseBody,
) = decodeFromString(deserializer, body.string())

internal suspend inline fun OkHttpClient.await(block: Request.Builder.() -> Unit): Response =
    newCall(Request(block)).await()

@OptIn(ExperimentalCoroutinesApi::class)
internal suspend fun Call.await(): Response = suspendCancellableCoroutine { continuation ->
    continuation.invokeOnCancellation { cancel() }
    val callback = object : Callback {

        override fun onResponse(call: Call, response: Response) {
            continuation.resume(response) { response.close() }
        }

        override fun onFailure(call: Call, e: IOException) {
            continuation.resumeWithException(e)
        }
    }
    enqueue(callback)
}

private inline fun Request(block: Request.Builder.() -> Unit): Request =
    Request.Builder().apply(block).build()

private val ApplicationJson by lazy { "application/json; charset=utf-8".toMediaType() }
