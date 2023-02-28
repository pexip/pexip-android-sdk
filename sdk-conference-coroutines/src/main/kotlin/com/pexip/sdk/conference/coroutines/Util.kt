/*
 * Copyright 2022-2023 Pexip AS
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
package com.pexip.sdk.conference.coroutines

import com.pexip.sdk.conference.Conference
import com.pexip.sdk.conference.ConferenceEvent
import com.pexip.sdk.conference.ConferenceEventListener
import com.pexip.sdk.conference.Message
import com.pexip.sdk.conference.MessageListener
import com.pexip.sdk.conference.MessageNotSentException
import com.pexip.sdk.conference.Messenger
import com.pexip.sdk.conference.SendCallback
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * Converts this [Conference] to a [Flow] that emits [ConferenceEvent]s.
 *
 * @return a flow of conference events
 */
public fun Conference.getConferenceEvents(): Flow<ConferenceEvent> = callbackFlow {
    val listener = ConferenceEventListener(::trySend)
    registerConferenceEventListener(listener)
    awaitClose { unregisterConferenceEventListener(listener) }
}

/**
 * Suspends until the message is sent or until an error is encountered.
 *
 * @param type a MIME type (e.g. "text/plain")
 * @param payload actual content of the message
 * @param participantId an optional unique identifier of the participant to send the message to
 * @return a message
 * @throws MessageNotSentException when there was an issue when sending the message
 */
public suspend fun Messenger.send(
    type: String,
    payload: String,
    participantId: UUID? = null,
): Message = suspendCoroutine {
    val callback = object : SendCallback {

        override fun onSuccess(message: Message) = it.resume(message)

        override fun onFailure(e: MessageNotSentException) = it.resumeWithException(e)
    }
    if (participantId == null) {
        send(type, payload, callback)
    } else {
        send(participantId, type, payload, callback)
    }
}

/**
 * Converts this [Messenger] to a [Flow] that emits [Message]s.
 *
 * @return a flow of messages
 */
public fun Messenger.getMessages(): Flow<Message> = callbackFlow {
    val listener = MessageListener(::trySend)
    registerMessageListener(listener)
    awaitClose { unregisterMessageListener(listener) }
}
