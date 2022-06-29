package com.pexip.sdk.api.coroutines

import com.pexip.sdk.api.Call
import com.pexip.sdk.api.Callback
import com.pexip.sdk.api.Event
import com.pexip.sdk.api.EventSource
import com.pexip.sdk.api.EventSourceFactory
import com.pexip.sdk.api.EventSourceListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Suspends until the [Call] completes with either success or failure.
 *
 * @param T successful response body type
 * @return successful response body
 */
public suspend fun <T> Call<T>.await(): T = suspendCancellableCoroutine {
    it.invokeOnCancellation { cancel() }
    val callback = object : Callback<T> {

        override fun onSuccess(call: Call<T>, response: T) = it.resume(response)

        override fun onFailure(call: Call<T>, t: Throwable) = it.resumeWithException(t)
    }
    enqueue(callback)
}

/**
 * Converts this [EventSourceFactory] to a [Flow].
 *
 * @return a [Flow] of [Event]s
 */
public fun EventSourceFactory.asFlow(): Flow<Event> = callbackFlow {
    val listener = object : EventSourceListener {

        override fun onOpen(eventSource: EventSource) {
            // noop
        }

        override fun onEvent(eventSource: EventSource, event: Event) {
            trySend(event)
        }

        override fun onClosed(eventSource: EventSource, t: Throwable?) {
            close(t)
        }
    }
    val source = create(listener)
    awaitClose { source.cancel() }
}
