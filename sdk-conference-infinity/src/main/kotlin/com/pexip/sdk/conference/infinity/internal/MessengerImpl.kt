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

import com.pexip.sdk.api.Call
import com.pexip.sdk.api.Callback
import com.pexip.sdk.api.Event
import com.pexip.sdk.api.EventSource
import com.pexip.sdk.api.EventSourceListener
import com.pexip.sdk.api.infinity.InfinityService
import com.pexip.sdk.api.infinity.MessageReceivedEvent
import com.pexip.sdk.api.infinity.MessageRequest
import com.pexip.sdk.api.infinity.TokenStore
import com.pexip.sdk.conference.Message
import com.pexip.sdk.conference.MessageListener
import com.pexip.sdk.conference.MessageNotSentException
import com.pexip.sdk.conference.Messenger
import com.pexip.sdk.conference.SendCallback
import java.util.UUID
import java.util.concurrent.CopyOnWriteArraySet

internal class MessengerImpl(
    private val senderId: UUID,
    private val senderName: String,
    private val store: TokenStore,
    private val step: InfinityService.ConferenceStep,
    private val atProvider: () -> Long = System::currentTimeMillis,
) : Messenger, EventSourceListener {

    private val listeners = CopyOnWriteArraySet<MessageListener>()

    override fun send(type: String, payload: String, callback: SendCallback) =
        sendInternal(type, payload, callback)

    override fun send(
        participantId: UUID,
        type: String,
        payload: String,
        callback: SendCallback,
    ) = sendInternal(type, payload, callback, participantId)

    override fun registerMessageListener(listener: MessageListener) {
        listeners += listener
    }

    override fun unregisterMessageListener(listener: MessageListener) {
        listeners -= listener
    }

    override fun onOpen(eventSource: EventSource) {
        // noop
    }

    override fun onEvent(eventSource: EventSource, event: Event) {
        if (event !is MessageReceivedEvent) return
        val message = Message(
            at = atProvider(),
            participantId = event.participantId,
            participantName = event.participantName,
            type = event.type,
            payload = event.payload,
            direct = event.direct,
        )
        listeners.forEach { it.onMessage(message) }
    }

    override fun onClosed(eventSource: EventSource, t: Throwable?) {
        // noop
    }

    private fun sendInternal(
        type: String,
        payload: String,
        callback: SendCallback,
        participantId: UUID? = null,
    ) {
        require(type.isNotBlank()) { "type is blank." }
        require(payload.isNotBlank()) { "payload is blank." }
        val request = MessageRequest(
            type = type,
            payload = payload,
        )
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
        val c = object : Callback<Boolean> {

            override fun onSuccess(call: Call<Boolean>, response: Boolean) = when (response) {
                true -> callback.onSuccess(message)
                else -> callback.onFailure(MessageNotSentException(message))
            }

            override fun onFailure(call: Call<Boolean>, t: Throwable) =
                callback.onFailure(MessageNotSentException(message, t))
        }
        call.enqueue(c)
    }
}
