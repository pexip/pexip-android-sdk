/*
 * Copyright 2022 Pexip AS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
