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
import com.pexip.sdk.api.EventSource
import com.pexip.sdk.api.infinity.InfinityService
import com.pexip.sdk.api.infinity.MessageReceivedEvent
import com.pexip.sdk.api.infinity.MessageRequest
import com.pexip.sdk.api.infinity.TokenStore
import com.pexip.sdk.conference.Message
import com.pexip.sdk.conference.MessageListener
import com.pexip.sdk.conference.MessageNotSentException
import com.pexip.sdk.conference.SendCallback
import java.util.UUID
import java.util.concurrent.CountDownLatch
import kotlin.random.Random
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.fail

class MessengerImplTest {

    private lateinit var store: TokenStore
    private lateinit var latch: CountDownLatch

    @BeforeTest
    fun setUp() {
        store = TokenStore.create(Random.nextToken())
        latch = CountDownLatch(1)
    }

    @Test
    fun `send() to all is successful`() {
        val expected = Random.nextMessage()
        val step = object : TestConferenceStep() {

            override fun message(request: MessageRequest, token: String): Call<Boolean> {
                assertEquals(expected.type, request.type)
                assertEquals(expected.payload, request.payload)
                return object : TestCall<Boolean> {

                    override fun enqueue(callback: Callback<Boolean>) =
                        callback.onSuccess(this, true)
                }
            }
        }
        val messenger = MessengerImpl(
            senderId = expected.participantId,
            senderName = expected.participantName,
            store = store,
            step = step,
            atProvider = expected::at,
        )
        val callback = object : TestSendCallback {

            override fun onSuccess(message: Message) {
                assertEquals(expected, message)
                latch.countDown()
            }
        }
        messenger.send(expected.type, expected.payload, callback)
        latch.await()
    }

    @Test
    fun `send() to all failed to send`() {
        val expected = Random.nextMessage()
        val step = object : TestConferenceStep() {

            override fun message(request: MessageRequest, token: String): Call<Boolean> {
                assertEquals(expected.type, request.type)
                assertEquals(expected.payload, request.payload)
                return object : TestCall<Boolean> {

                    override fun enqueue(callback: Callback<Boolean>) =
                        callback.onSuccess(this, false)
                }
            }
        }
        val messenger = MessengerImpl(
            senderId = expected.participantId,
            senderName = expected.participantName,
            store = store,
            step = step,
            atProvider = expected::at,
        )
        val callback = object : TestSendCallback {

            override fun onFailure(e: MessageNotSentException) {
                assertEquals(expected, e.msg)
                assertNull(e.cause)
                latch.countDown()
            }
        }
        messenger.send(expected.type, expected.payload, callback)
        latch.await()
    }

    @Test
    fun `send() to all throws`() {
        val expected = Random.nextMessage()
        val expectedThrowable = Throwable()
        val step = object : TestConferenceStep() {

            override fun message(request: MessageRequest, token: String): Call<Boolean> {
                assertEquals(expected.type, request.type)
                assertEquals(expected.payload, request.payload)
                return object : TestCall<Boolean> {

                    override fun enqueue(callback: Callback<Boolean>) =
                        callback.onFailure(this, expectedThrowable)
                }
            }
        }
        val messenger = MessengerImpl(
            senderId = expected.participantId,
            senderName = expected.participantName,
            store = store,
            step = step,
            atProvider = expected::at,
        )
        val callback = object : TestSendCallback {

            override fun onFailure(e: MessageNotSentException) {
                assertEquals(expected, e.msg)
                assertEquals(expectedThrowable, e.cause)
                latch.countDown()
            }
        }
        messenger.send(expected.type, expected.payload, callback)
        latch.await()
    }

    @Test
    fun `send() to a participant is successful`() {
        val expected = Random.nextMessage(direct = true)
        val id = UUID.randomUUID()
        val step = object : TestConferenceStep() {

            override fun participant(participantId: UUID): InfinityService.ParticipantStep {
                assertEquals(id, participantId)
                return object : TestParticipantStep() {

                    override fun message(request: MessageRequest, token: String): Call<Boolean> {
                        assertEquals(expected.type, request.type)
                        assertEquals(expected.payload, request.payload)
                        return object : TestCall<Boolean> {

                            override fun enqueue(callback: Callback<Boolean>) =
                                callback.onSuccess(this, true)
                        }
                    }
                }
            }
        }
        val messenger = MessengerImpl(
            senderId = expected.participantId,
            senderName = expected.participantName,
            store = store,
            step = step,
            atProvider = expected::at,
        )
        val callback = object : TestSendCallback {

            override fun onSuccess(message: Message) {
                assertEquals(expected, message)
                latch.countDown()
            }
        }
        messenger.send(id, expected.type, expected.payload, callback)
        latch.await()
    }

    @Test
    fun `send() to a participant failed to send`() {
        val expected = Random.nextMessage(direct = true)
        val id = UUID.randomUUID()
        val step = object : TestConferenceStep() {

            override fun participant(participantId: UUID): InfinityService.ParticipantStep {
                assertEquals(id, participantId)
                return object : TestParticipantStep() {

                    override fun message(request: MessageRequest, token: String): Call<Boolean> {
                        assertEquals(expected.type, request.type)
                        assertEquals(expected.payload, request.payload)
                        return object : TestCall<Boolean> {

                            override fun enqueue(callback: Callback<Boolean>) =
                                callback.onSuccess(this, false)
                        }
                    }
                }
            }
        }
        val messenger = MessengerImpl(
            senderId = expected.participantId,
            senderName = expected.participantName,
            store = store,
            step = step,
            atProvider = expected::at,
        )
        val callback = object : TestSendCallback {

            override fun onFailure(e: MessageNotSentException) {
                assertEquals(expected, e.msg)
                assertNull(e.cause)
                latch.countDown()
            }
        }
        messenger.send(id, expected.type, expected.payload, callback)
        latch.await()
    }

    @Test
    fun `send() to a participant throws`() {
        val expected = Random.nextMessage(direct = true)
        val expectedThrowable = Throwable()
        val id = UUID.randomUUID()
        val step = object : TestConferenceStep() {

            override fun participant(participantId: UUID): InfinityService.ParticipantStep {
                assertEquals(id, participantId)
                return object : TestParticipantStep() {

                    override fun message(request: MessageRequest, token: String): Call<Boolean> {
                        assertEquals(expected.type, request.type)
                        assertEquals(expected.payload, request.payload)
                        return object : TestCall<Boolean> {

                            override fun enqueue(callback: Callback<Boolean>) =
                                callback.onFailure(this, expectedThrowable)
                        }
                    }
                }
            }
        }
        val messenger = MessengerImpl(
            senderId = expected.participantId,
            senderName = expected.participantName,
            store = store,
            step = step,
            atProvider = expected::at,
        )
        val callback = object : TestSendCallback {

            override fun onFailure(e: MessageNotSentException) {
                assertEquals(expected, e.msg)
                assertEquals(expectedThrowable, e.cause)
                latch.countDown()
            }
        }
        messenger.send(id, expected.type, expected.payload, callback)
        latch.await()
    }

    @Test
    fun `onEvent maps MessageReceivedEvent to Message`() {
        val at = System.currentTimeMillis()
        val messenger = MessengerImpl(
            senderId = UUID.randomUUID(),
            senderName = Random.nextString(8),
            store = store,
            step = object : TestConferenceStep() {},
            atProvider = { at },
        )
        val message = Random.nextMessage(at = at, direct = false)
        val directMessage = Random.nextMessage(at = at, direct = true)
        val messages = mutableListOf<Message>()
        val listener = MessageListener(messages::add)
        val eventSource = EventSource(::fail)
        messenger.registerMessengerListener(listener)
        messenger.onEvent(eventSource, message.toMessageReceivedEvent())
        messenger.onEvent(eventSource, directMessage.toMessageReceivedEvent())
        messenger.unregisterMessengerListener(listener)
        assertEquals(listOf(message, directMessage), messages)
    }

    private fun Random.nextMessage(at: Long = System.currentTimeMillis(), direct: Boolean = false) =
        Message(
            at = at,
            participantId = UUID.randomUUID(),
            participantName = nextString(8),
            type = nextString(8),
            payload = nextString(64),
            direct = direct,
        )

    private fun Message.toMessageReceivedEvent() = MessageReceivedEvent(
        participantId = participantId,
        participantName = participantName,
        type = type,
        payload = payload,
        direct = direct,
    )

    private interface TestSendCallback : SendCallback {

        override fun onSuccess(message: Message): Unit = fail()

        override fun onFailure(e: MessageNotSentException): Unit = fail()
    }
}
