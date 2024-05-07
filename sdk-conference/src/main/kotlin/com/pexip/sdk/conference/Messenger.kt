/*
 * Copyright 2023-2024 Pexip AS
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

import com.pexip.sdk.infinity.ParticipantId
import kotlinx.coroutines.flow.Flow

/**
 * Handles sending and receiving messages.
 *
 * @property message a [Flow] of incoming [Message]s
 */
public interface Messenger {

    public val message: Flow<Message>

    /**
     * Suspends until the message is sent or until an error is encountered.
     *
     * @param type a MIME type (e.g. "text/plain")
     * @param payload actual content of the message
     * @param participantId an optional unique identifier of the participant to send the message to
     * @return a message
     * @throws MessageNotSentException when there was an issue when sending the message
     */
    public suspend fun send(
        type: String,
        payload: String,
        participantId: ParticipantId? = null,
    ): Message
}
