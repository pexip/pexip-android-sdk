/*
 * Copyright 2023 Pexip AS
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
package com.pexip.sdk.conference

import java.util.UUID

/**
 * Handles sending and receiving messages.
 */
public interface Messenger {

    /**
     * Sends a message to all participants in the conference.
     *
     * @param type a MIME type (e.g. "text/plain")
     * @param payload actual content of the message
     * @param callback a callback to be invoked upon completion
     */
    public fun send(type: String, payload: String, callback: SendCallback)

    /**
     * Sends a message to a specific participant in the conference.
     *
     * @param participantId a unique identifier of the participant
     * @param type a MIME type (e.g. "text/plain")
     * @param payload actual content of the message
     * @param callback a callback to be invoked upon completion
     */
    public fun send(participantId: UUID, type: String, payload: String, callback: SendCallback)

    /**
     * Registers a [MessageListener].
     *
     * @param listener a message listener
     */
    public fun registerMessengerListener(listener: MessageListener)

    /**
     * Unregisters a [MessageListener].
     *
     * @param listener a message listener
     */
    public fun unregisterMessengerListener(listener: MessageListener)
}
