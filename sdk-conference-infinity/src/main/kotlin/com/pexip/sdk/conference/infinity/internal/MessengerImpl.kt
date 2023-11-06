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
package com.pexip.sdk.conference.infinity.internal

import com.pexip.sdk.api.Event
import com.pexip.sdk.api.coroutines.await
import com.pexip.sdk.api.infinity.InfinityService
import com.pexip.sdk.api.infinity.MessageReceivedEvent
import com.pexip.sdk.api.infinity.MessageRequest
import com.pexip.sdk.api.infinity.TokenStore
import com.pexip.sdk.conference.Message
import com.pexip.sdk.conference.MessageListener
import com.pexip.sdk.conference.MessageNotSentException
import com.pexip.sdk.conference.Messenger
import com.pexip.sdk.conference.SendCallback
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.UUID
import java.util.concurrent.CopyOnWriteArraySet

internal class MessengerImpl(
    private val scope: CoroutineScope,
    event: Flow<Event>,
    private val senderId: UUID,
    private val senderName: String,
    private val store: TokenStore,
    private val step: InfinityService.ConferenceStep,
    private val atProvider: () -> Long = System::currentTimeMillis,
) : Messenger {

    private val listeners = CopyOnWriteArraySet<MessageListener>()

    override val message: Flow<Message> = event
        .filterIsInstance<MessageReceivedEvent>()
        .map {
            Message(
                at = atProvider(),
                participantId = it.participantId,
                participantName = it.participantName,
                type = it.type,
                payload = it.payload,
                direct = it.direct,
            )
        }

    init {
        message
            .onEach { message -> listeners.forEach { it.onMessage(message) } }
            .flowOn(Dispatchers.Main.immediate)
            .launchIn(scope)
    }

    override suspend fun send(type: String, payload: String, participantId: UUID?): Message {
        val request = MessageRequest(type = type, payload = payload)
        val token = store.get()
        val call = when (participantId) {
            null -> step.message(request, token)
            else -> step.participant(participantId).message(request, token)
        }
        val message = Message(
            at = atProvider(),
            participantId = senderId,
            participantName = senderName,
            type = type,
            payload = payload,
            direct = participantId != null,
        )
        val result = try {
            call.await()
        } catch (e: CancellationException) {
            throw e
        } catch (t: Throwable) {
            throw MessageNotSentException(message, t)
        }
        return when (result) {
            true -> message
            else -> throw MessageNotSentException(message)
        }
    }

    @Deprecated(message = "Use the coroutines version of this method.")
    override fun send(type: String, payload: String, callback: SendCallback) =
        sendInternal(type, payload, callback)

    @Deprecated(message = "Use the coroutines version of this method.")
    override fun send(
        participantId: UUID,
        type: String,
        payload: String,
        callback: SendCallback,
    ) = sendInternal(type, payload, callback, participantId)

    @Deprecated(message = "Use getMessages() that returns a Flow")
    override fun registerMessageListener(listener: MessageListener) {
        listeners += listener
    }

    @Deprecated(message = "Use getMessages() that returns a Flow")
    override fun unregisterMessageListener(listener: MessageListener) {
        listeners -= listener
    }

    private fun sendInternal(
        type: String,
        payload: String,
        callback: SendCallback,
        participantId: UUID? = null,
    ) {
        scope.launch {
            try {
                val message = send(type, payload, participantId)
                callback.onSuccess(message)
            } catch (e: MessageNotSentException) {
                callback.onFailure(e)
            }
        }
    }
}
