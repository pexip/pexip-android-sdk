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
package com.pexip.sdk.conference.infinity.internal

import com.pexip.sdk.api.infinity.DataChannelMessage
import com.pexip.sdk.conference.Message
import com.pexip.sdk.conference.MessageNotSentException
import com.pexip.sdk.infinity.ParticipantId
import com.pexip.sdk.media.Data
import com.pexip.sdk.media.DataChannel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn

internal class DataChannelMessengerImpl(
    scope: CoroutineScope,
    private val senderId: ParticipantId,
    private val senderName: String,
    private val dataChannel: DataChannel,
    private val atProvider: () -> Long = System::currentTimeMillis,
) : AbstractMessenger(scope) {

    override val message: Flow<Message> = dataChannel.data
        .mapNotNull(::toMessage)
        .shareIn(scope, SharingStarted.Eagerly)

    init {
        message
            .onEach(::onMessage)
            .flowOn(Dispatchers.Main.immediate)
            .launchIn(scope)
    }

    override suspend fun send(
        type: String,
        payload: String,
        participantId: ParticipantId?,
    ): Message {
        val message = Message(
            at = atProvider(),
            participantId = senderId,
            participantName = senderName,
            type = type,
            payload = payload,
            direct = participantId != null,
        )
        return try {
            val m = DataChannelMessage.Message(
                body = DataChannelMessage.Message.Body(
                    senderId = message.participantId,
                    senderName = message.participantName,
                    type = message.type,
                    payload = message.payload,
                ),
            )
            val json = m.encodeToString()
            val data = Data(json.toByteArray(), false)
            dataChannel.send(data)
            message
        } catch (e: CancellationException) {
            throw e
        } catch (t: Throwable) {
            throw MessageNotSentException(message, t)
        }
    }

    private fun toMessage(data: Data): Message? {
        if (data.binary) return null
        val message = try {
            DataChannelMessage.decodeFromString(data.data.decodeToString())
        } catch (e: Exception) {
            return null
        }
        return when (message) {
            is DataChannelMessage.Message -> Message(
                at = atProvider(),
                participantId = message.body.senderId,
                participantName = message.body.senderName,
                type = message.body.type,
                payload = message.body.payload,
                direct = false,
            )
            is DataChannelMessage.Unknown -> null
        }
    }
}
