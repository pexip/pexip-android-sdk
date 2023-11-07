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

import com.pexip.sdk.conference.Message
import com.pexip.sdk.conference.MessageNotSentException
import com.pexip.sdk.media.Data
import com.pexip.sdk.media.DataChannel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID

internal class DataChannelMessengerImpl(
    scope: CoroutineScope,
    private val senderId: UUID,
    private val senderName: String,
    private val dataChannel: DataChannel,
    private val atProvider: () -> Long = System::currentTimeMillis,
) : AbstractMessenger(scope) {

    override val message: Flow<Message> = dataChannel.data.mapNotNull(::onData)

    init {
        message
            .onEach(::onMessage)
            .flowOn(Dispatchers.Main.immediate)
            .launchIn(scope)
    }

    override suspend fun send(type: String, payload: String, participantId: UUID?): Message {
        val message = Message(
            at = atProvider(),
            participantId = senderId,
            participantName = senderName,
            type = type,
            payload = payload,
            direct = participantId != null,
        )
        return try {
            val m = Box(
                type = BoxType.MESSAGE,
                body = MessageBody(
                    type = message.type,
                    uuid = message.participantId,
                    origin = message.participantName,
                    payload = message.payload,
                ),
            )
            val json = Json.encodeToString(m)
            val data = Data(json.toByteArray(), false)
            dataChannel.send(data)
            message
        } catch (e: CancellationException) {
            throw e
        } catch (t: Throwable) {
            throw MessageNotSentException(message, t)
        }
    }

    private fun onData(data: Data): Message? {
        if (data.binary) return null
        val (type, body) = try {
            Json.decodeFromString<Box<MessageBody>>(data.data.decodeToString())
        } catch (e: SerializationException) {
            return null
        }
        return when (type) {
            BoxType.MESSAGE -> Message(
                at = atProvider(),
                participantId = body.uuid,
                participantName = body.origin,
                type = body.type,
                payload = body.payload,
                direct = false,
            )
        }
    }

    companion object {

        val Json = Json { ignoreUnknownKeys = true }
    }
}
