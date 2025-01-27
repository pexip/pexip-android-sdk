/*
 * Copyright 2022-2024 Pexip AS
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
package com.pexip.sdk.api

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

public fun interface EventSourceFactory {

    /**
     * Creates a new event source and immediately returns it. Creating an event source initiates an
     * asynchronous process to connect the socket. Once that succeeds or fails, `listener` will be
     * notified. The caller must cancel the returned event source when it is no longer in use.
     */
    public fun create(listener: EventSourceListener): EventSource

    /**
     * Converts this [EventSourceFactory] to a [Flow].
     *
     * @return a [Flow] of [Event]s
     */
    public fun asFlow(): Flow<Event> = callbackFlow {
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
}
