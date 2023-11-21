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
import assertk.all
import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNull
import assertk.assertions.prop
import com.pexip.sdk.api.Call
import com.pexip.sdk.api.Callback
import com.pexip.sdk.api.Event
import com.pexip.sdk.api.infinity.InfinityService
import com.pexip.sdk.api.infinity.MessageReceivedEvent
import com.pexip.sdk.api.infinity.MessageRequest
import com.pexip.sdk.api.infinity.TokenStore
import com.pexip.sdk.conference.MessageNotSentException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.runTest
import java.util.UUID
import kotlin.random.Random
import kotlin.test.BeforeTest
import kotlin.test.Test

class MessengerImplTest {

    private lateinit var event: MutableSharedFlow<Event>
    private lateinit var store: TokenStore

    @BeforeTest
    fun setUp() {
        event = MutableSharedFlow(extraBufferCapacity = 1)
        store = TokenStore.create(Random.nextToken())
    }

    @Test
    fun `send() to all is successful`() = runTest {
        val expected = Random.nextMessage()
        val step = object : TestConferenceStep() {

            override fun message(request: MessageRequest, token: String): Call<Boolean> {
                assertThat(request::type).isEqualTo(expected.type)
                assertThat(request::payload).isEqualTo(expected.payload)
                return object : TestCall<Boolean> {

                    override fun enqueue(callback: Callback<Boolean>) =
                        callback.onSuccess(this, true)
                }
            }
        }
        val messenger = MessengerImpl(
            scope = backgroundScope,
            event = event,
            senderId = expected.participantId,
            senderName = expected.participantName,
            store = store,
            step = step,
            atProvider = expected::at,
        )
        val message = messenger.send(expected.type, expected.payload)
        assertThat(message, "message").isEqualTo(expected)
    }

    @Test
    fun `send() to all failed to send`() = runTest {
        val expected = Random.nextMessage()
        val step = object : TestConferenceStep() {

            override fun message(request: MessageRequest, token: String): Call<Boolean> {
                assertThat(request::type).isEqualTo(expected.type)
                assertThat(request::payload).isEqualTo(expected.payload)
                return object : TestCall<Boolean> {

                    override fun enqueue(callback: Callback<Boolean>) =
                        callback.onSuccess(this, false)
                }
            }
        }
        val messenger = MessengerImpl(
            scope = backgroundScope,
            event = event,
            senderId = expected.participantId,
            senderName = expected.participantName,
            store = store,
            step = step,
            atProvider = expected::at,
        )
        assertFailure { messenger.send(expected.type, expected.payload) }
            .isInstanceOf<MessageNotSentException>()
            .all {
                prop(MessageNotSentException::msg).isEqualTo(expected)
                prop(MessageNotSentException::cause).isNull()
            }
    }

    @Test
    fun `send() to all throws`() = runTest {
        val expected = Random.nextMessage()
        val expectedThrowable = Throwable()
        val step = object : TestConferenceStep() {

            override fun message(request: MessageRequest, token: String): Call<Boolean> {
                assertThat(request::type).isEqualTo(expected.type)
                assertThat(request::payload).isEqualTo(expected.payload)
                return object : TestCall<Boolean> {

                    override fun enqueue(callback: Callback<Boolean>) =
                        callback.onFailure(this, expectedThrowable)
                }
            }
        }
        val messenger = MessengerImpl(
            scope = backgroundScope,
            event = event,
            senderId = expected.participantId,
            senderName = expected.participantName,
            store = store,
            step = step,
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
    fun `send() to a participant is successful`() = runTest {
        val expected = Random.nextMessage(direct = true)
        val id = UUID.randomUUID()
        val step = object : TestConferenceStep() {

            override fun participant(participantId: UUID): InfinityService.ParticipantStep {
                assertThat(participantId, "participantId").isEqualTo(id)
                return object : TestParticipantStep() {

                    override fun message(request: MessageRequest, token: String): Call<Boolean> {
                        assertThat(request::type).isEqualTo(expected.type)
                        assertThat(request::payload).isEqualTo(expected.payload)
                        return object : TestCall<Boolean> {

                            override fun enqueue(callback: Callback<Boolean>) =
                                callback.onSuccess(this, true)
                        }
                    }
                }
            }
        }
        val messenger = MessengerImpl(
            scope = backgroundScope,
            event = event,
            senderId = expected.participantId,
            senderName = expected.participantName,
            store = store,
            step = step,
            atProvider = expected::at,
        )
        val message = messenger.send(expected.type, expected.payload, id)
        assertThat(message, "message").isEqualTo(expected)
    }

    @Test
    fun `send() to a participant failed to send`() = runTest {
        val expected = Random.nextMessage(direct = true)
        val id = UUID.randomUUID()
        val step = object : TestConferenceStep() {

            override fun participant(participantId: UUID): InfinityService.ParticipantStep {
                assertThat(participantId, "participantId").isEqualTo(id)
                return object : TestParticipantStep() {

                    override fun message(request: MessageRequest, token: String): Call<Boolean> {
                        assertThat(request::type).isEqualTo(expected.type)
                        assertThat(request::payload).isEqualTo(expected.payload)
                        return object : TestCall<Boolean> {

                            override fun enqueue(callback: Callback<Boolean>) =
                                callback.onSuccess(this, false)
                        }
                    }
                }
            }
        }
        val messenger = MessengerImpl(
            scope = backgroundScope,
            event = event,
            senderId = expected.participantId,
            senderName = expected.participantName,
            store = store,
            step = step,
            atProvider = expected::at,
        )
        assertFailure { messenger.send(expected.type, expected.payload, id) }
            .isInstanceOf<MessageNotSentException>()
            .all {
                prop(MessageNotSentException::msg).isEqualTo(expected)
                prop(MessageNotSentException::cause).isNull()
            }
    }

    @Test
    fun `send() to a participant throws`() = runTest {
        val expected = Random.nextMessage(direct = true)
        val expectedThrowable = Throwable()
        val id = UUID.randomUUID()
        val step = object : TestConferenceStep() {

            override fun participant(participantId: UUID): InfinityService.ParticipantStep {
                assertThat(participantId, "participantId").isEqualTo(id)
                return object : TestParticipantStep() {

                    override fun message(request: MessageRequest, token: String): Call<Boolean> {
                        assertThat(request::type).isEqualTo(expected.type)
                        assertThat(request::payload).isEqualTo(expected.payload)
                        return object : TestCall<Boolean> {

                            override fun enqueue(callback: Callback<Boolean>) =
                                callback.onFailure(this, expectedThrowable)
                        }
                    }
                }
            }
        }
        val messenger = MessengerImpl(
            scope = backgroundScope,
            event = event,
            senderId = expected.participantId,
            senderName = expected.participantName,
            store = store,
            step = step,
            atProvider = expected::at,
        )
        assertFailure { messenger.send(expected.type, expected.payload, id) }
            .isInstanceOf<MessageNotSentException>()
            .all {
                prop(MessageNotSentException::msg).isEqualTo(expected)
                prop(MessageNotSentException::cause).isEqualTo(expectedThrowable)
            }
    }

    @Test
    fun `onEvent maps MessageReceivedEvent to Message`() = runTest {
        val at = System.currentTimeMillis()
        val messenger = MessengerImpl(
            scope = backgroundScope,
            event = event,
            senderId = UUID.randomUUID(),
            senderName = Random.nextString(8),
            store = store,
            step = object : TestConferenceStep() {},
            atProvider = { at },
        )
        messenger.message.test {
            val messages = List(10) { Random.nextMessage(at = at, direct = it % 2 == 0) }
            messages.forEach {
                val e = MessageReceivedEvent(
                    participantId = it.participantId,
                    participantName = it.participantName,
                    type = it.type,
                    payload = it.payload,
                    direct = it.direct,
                )
                event.emit(e)
                assertThat(awaitItem(), "message").isEqualTo(it)
            }
        }
    }
}
