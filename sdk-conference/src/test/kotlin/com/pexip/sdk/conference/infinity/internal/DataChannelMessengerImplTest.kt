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

import app.cash.turbine.test
import assertk.Assert
import assertk.all
import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.prop
import assertk.fail
import com.pexip.sdk.api.infinity.DataChannelMessage
import com.pexip.sdk.conference.Message
import com.pexip.sdk.conference.MessageNotSentException
import com.pexip.sdk.media.Data
import com.pexip.sdk.media.DataChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import java.util.UUID
import kotlin.random.Random
import kotlin.test.Test

class DataChannelMessengerImplTest {

    @Test
    fun `send() throws`() = runTest {
        val expected = Random.nextMessage()
        val expectedThrowable = Throwable()
        val dataChannel = object : DataChannel {

            override val id: Int
                get() = Random.nextInt()

            override val data: Flow<Data> = emptyFlow()

            override suspend fun send(data: Data) {
                assertThat(data).correspondsTo(expected)
                throw expectedThrowable
            }
        }
        val messenger = DataChannelMessengerImpl(
            scope = backgroundScope,
            senderId = expected.participantId,
            senderName = expected.participantName,
            dataChannel = dataChannel,
            atProvider = expected::at,
        )
        assertFailure { messenger.send(expected.type, expected.payload) }
            .isInstanceOf<MessageNotSentException>()
            .all {
                prop(MessageNotSentException::msg).isEqualTo(expected)
                prop(MessageNotSentException::cause).isEqualTo(expectedThrowable)
            }
    }

    @Test
    fun `send() returns Message`() = runTest {
        val expected = Random.nextMessage()
        val dataChannel = object : DataChannel {

            override val id: Int
                get() = Random.nextInt()

            override val data: Flow<Data> = emptyFlow()

            override suspend fun send(data: Data) {
                assertThat(data).correspondsTo(expected)
            }
        }
        val messenger = DataChannelMessengerImpl(
            scope = backgroundScope,
            senderId = expected.participantId,
            senderName = expected.participantName,
            dataChannel = dataChannel,
            atProvider = expected::at,
        )
        assertThat(messenger.send(expected.type, expected.payload), "message")
            .isEqualTo(expected)
    }

    @Test
    fun `onData ignores binary messages`() = runTest {
        val flow = MutableSharedFlow<Data>()
        val dataChannel = object : DataChannel {

            override val id: Int
                get() = Random.nextInt()

            override val data: Flow<Data> = flow

            override suspend fun send(data: Data): Unit = fail("")
        }
        val messenger = DataChannelMessengerImpl(
            scope = backgroundScope,
            senderId = UUID.randomUUID(),
            senderName = Random.nextString(8),
            dataChannel = dataChannel,
            atProvider = { fail("unexpected atProvider()") },
        )
        messenger.message.test {
            val messages = List(10) { Data(Random.nextBytes(8), true) }
            messages.forEach { flow.emit(it) }
            expectNoEvents()
        }
    }

    @Test
    fun `onData ignores malformed messages`() = runTest {
        val flow = MutableSharedFlow<Data>()
        val dataChannel = object : DataChannel {

            override val id: Int
                get() = Random.nextInt()

            override val data: Flow<Data> = flow

            override suspend fun send(data: Data): Unit = fail("")
        }
        val messenger = DataChannelMessengerImpl(
            scope = backgroundScope,
            senderId = UUID.randomUUID(),
            senderName = Random.nextString(8),
            dataChannel = dataChannel,
            atProvider = { fail("unexpected atProvider()") },
        )
        messenger.message.test {
            val messages = List(10) { Data(Random.nextBytes(8), false) }
            messages.forEach { flow.emit(it) }
            expectNoEvents()
        }
    }

    @Test
    fun `onData maps Data to Message`() = runTest {
        val at = System.currentTimeMillis()
        val flow = MutableSharedFlow<Data>()
        val dataChannel = object : DataChannel {

            override val id: Int
                get() = Random.nextInt()

            override val data: Flow<Data> = flow

            override suspend fun send(data: Data): Unit = fail("")
        }
        val messenger = DataChannelMessengerImpl(
            scope = backgroundScope,
            senderId = UUID.randomUUID(),
            senderName = Random.nextString(8),
            dataChannel = dataChannel,
            atProvider = { at },
        )
        messenger.message.test {
            val strings = List(10) { "{\"type\": \"${Random.nextString(8)}\"}" }
            strings.forEach {
                val data = Data(it.encodeToByteArray(), false)
                flow.emit(data)
            }
            expectNoEvents()
            val messages = List(10) { Random.nextMessage(at) }
            messages.forEach {
                val message = DataChannelMessage.Message(
                    body = DataChannelMessage.Message.Body(
                        type = it.type,
                        payload = it.payload,
                        senderId = it.participantId,
                        senderName = it.participantName,
                    ),
                )
                val json = message.encodeToString()
                val data = Data(
                    data = json.encodeToByteArray(),
                    binary = false,
                )
                flow.emit(data)
                assertThat(awaitItem(), "message").isEqualTo(it)
            }
        }
    }

    private fun Assert<Data>.correspondsTo(message: Message) = all {
        prop(Data::binary).isFalse()
        val body = DataChannelMessage.Message.Body(
            type = message.type,
            payload = message.payload,
            senderId = message.participantId,
            senderName = message.participantName,
        )
        prop(Data::data)
            .transform { DataChannelMessage.decodeFromString(it.decodeToString()) }
            .isInstanceOf<DataChannelMessage.Message>()
            .prop(DataChannelMessage.Message::body)
            .isEqualTo(body)
    }
}
