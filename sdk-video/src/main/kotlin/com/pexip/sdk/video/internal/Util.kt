package com.pexip.sdk.video.internal

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
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
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
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

internal fun EventSource.Factory.events(request: Request): Flow<Event> = callbackFlow {
    val listener = object : EventSourceListener() {

        override fun onEvent(eventSource: EventSource, id: String?, type: String?, data: String) {
            trySend(Event(id, type, data))
        }

        override fun onFailure(eventSource: EventSource, t: Throwable?, response: Response?) {
            close(t)
        }
    }
    val source = newEventSource(request, listener)
    awaitClose { source.cancel() }
}

internal inline fun Request(block: Request.Builder.() -> Unit): Request =
    Request.Builder().apply(block).build()

private val ApplicationJson by lazy { "application/json; charset=utf-8".toMediaType() }
