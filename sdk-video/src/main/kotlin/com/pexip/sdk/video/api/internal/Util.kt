package com.pexip.sdk.video.api.internal

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.ResponseBody
import java.io.IOException
import kotlin.coroutines.resumeWithException

internal suspend inline fun <reified T> Json.encodeToRequestBody(value: T) =
    withContext(Dispatchers.IO) { encodeToString(value).toRequestBody(OkHttpInfinityService.ApplicationJson) }

@OptIn(ExperimentalSerializationApi::class)
internal suspend inline fun <reified T> Json.decodeFromResponseBody(body: ResponseBody) =
    withContext(Dispatchers.IO) { decodeFromStream<T>(body.byteStream()) }

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
