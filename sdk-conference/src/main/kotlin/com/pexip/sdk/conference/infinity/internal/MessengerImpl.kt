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
import com.pexip.sdk.conference.MessageNotSentException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import java.util.UUID

internal class MessengerImpl(
    scope: CoroutineScope,
    event: Flow<Event>,
    private val senderId: UUID,
    private val senderName: String,
    private val store: TokenStore,
    private val step: InfinityService.ConferenceStep,
    private val atProvider: () -> Long = System::currentTimeMillis,
) : AbstractMessenger(scope) {

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
            .onEach(::onMessage)
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
}
