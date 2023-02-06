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
package com.pexip.sdk.api

public interface EventSourceListener {

    /**
     * Invoked when an event source has been accepted by the remote peer and may begin transmitting
     * events.
     */
    public fun onOpen(eventSource: EventSource)

    /**
     * Invoked when a new [Event] has been received.
     */
    public fun onEvent(eventSource: EventSource, event: Event)

    /**
     * Invoked when an event source has been closed. Incoming events may have been lost.
     * No further calls to this listener will be made.
     */
    public fun onClosed(eventSource: EventSource, t: Throwable?)
}
