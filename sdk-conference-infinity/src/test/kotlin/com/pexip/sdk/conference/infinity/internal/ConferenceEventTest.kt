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
package com.pexip.sdk.conference.infinity.internal

import app.cash.turbine.test
import assertk.Assert
import assertk.all
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.prop
import com.pexip.sdk.api.Event
import com.pexip.sdk.api.EventSource
import com.pexip.sdk.api.EventSourceFactory
import com.pexip.sdk.api.EventSourceListener
import com.pexip.sdk.api.infinity.ByeEvent
import com.pexip.sdk.api.infinity.DisconnectEvent
import com.pexip.sdk.api.infinity.MessageReceivedEvent
import com.pexip.sdk.api.infinity.PresentationStartEvent
import com.pexip.sdk.api.infinity.PresentationStopEvent
import com.pexip.sdk.api.infinity.ReferEvent
import com.pexip.sdk.api.infinity.Token
import com.pexip.sdk.api.infinity.TokenStore
import com.pexip.sdk.conference.ConferenceEvent
import com.pexip.sdk.conference.DisconnectConferenceEvent
import com.pexip.sdk.conference.FailureConferenceEvent
import com.pexip.sdk.conference.MessageReceivedConferenceEvent
import com.pexip.sdk.conference.PresentationStartConferenceEvent
import com.pexip.sdk.conference.PresentationStopConferenceEvent
import com.pexip.sdk.conference.ReferConferenceEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import java.util.UUID
import kotlin.properties.Delegates
import kotlin.random.Random
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ConferenceEventTest {

    private var at by Delegates.notNull<Long>()
    private lateinit var event: MutableSharedFlow<Result<Event>>
    private lateinit var store: TokenStore

    @BeforeTest
    fun setUp() {
        at = Random.nextLong(Long.MAX_VALUE)
        event = MutableSharedFlow()
        store = TokenStore.create(Random.nextToken())
    }

    @Test
    fun `maps Event to ConferenceEvent`() = runTest {
        val step = testConferenceStep()
        step.conferenceEvent(store) { at }.test {
            event.awaitSubscribers()
            val presentationStartEvent = PresentationStartEvent(
                presenterId = UUID.randomUUID(),
                presenterName = Random.nextString(8),
            )
            event.emit(Result.success(presentationStartEvent))
            assertThat(awaitItem(), "presentationStartEvent")
                .isInstanceOf<PresentationStartConferenceEvent>()
                .all {
                    at(at)
                    prop(PresentationStartConferenceEvent::presenterId)
                        .isEqualTo(presentationStartEvent.presenterId)
                    prop(PresentationStartConferenceEvent::presenterName)
                        .isEqualTo(presentationStartEvent.presenterName)
                }
            val presentationStopEvent = PresentationStopEvent
            event.emit(Result.success(presentationStopEvent))
            assertThat(awaitItem(), "presentationStopEvent")
                .isInstanceOf<PresentationStopConferenceEvent>()
                .at(at)
            val referEvent = ReferEvent(
                conferenceAlias = Random.nextString(8),
                token = Random.nextString(8),
            )
            event.emit(Result.success(referEvent))
            assertThat(awaitItem(), "referEvent")
                .isInstanceOf<ReferConferenceEvent>()
                .all {
                    at(at)
                    prop(ReferConferenceEvent::conferenceAlias).isEqualTo(referEvent.conferenceAlias)
                    prop(ReferConferenceEvent::token).isEqualTo(referEvent.token)
                }
            val disconnectEvent = DisconnectEvent(Random.nextString(8))
            event.emit(Result.success(disconnectEvent))
            assertThat(awaitItem(), "disconnectEvent")
                .isInstanceOf<DisconnectConferenceEvent>()
                .all {
                    at(at)
                    prop(DisconnectConferenceEvent::reason).isEqualTo(disconnectEvent.reason)
                }
            val byeEvent = ByeEvent
            event.emit(Result.success(byeEvent))
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `failure restarts the flow`() = runTest {
        val step = testConferenceStep()
        step.conferenceEvent(store) { at }.test {
            event.awaitSubscribers()
            val presentationStopEvent = PresentationStopEvent
            event.emit(Result.success(presentationStopEvent))
            assertThat(awaitItem(), "presentationStopEvent")
                .isInstanceOf<PresentationStopConferenceEvent>()
                .at(at)
            event.emit(Result.failure(Throwable()))
            event.awaitSubscribers()
            event.emit(Result.success(presentationStopEvent))
            assertThat(awaitItem(), "presentationStopEvent")
                .isInstanceOf<PresentationStopConferenceEvent>()
                .at(at)
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `returns ConferenceEvent if type is registered`() {
        val at = Random.nextLong(Long.MAX_VALUE)
        val testCases = buildMap {
            val presentationStartEvent = PresentationStartEvent(
                presenterId = UUID.randomUUID(),
                presenterName = Random.nextString(8),
            )
            this[presentationStartEvent] = PresentationStartConferenceEvent(
                at = at,
                presenterId = presentationStartEvent.presenterId,
                presenterName = presentationStartEvent.presenterName,
            )
            this[PresentationStopEvent] = PresentationStopConferenceEvent(at)
            val messageReceivedEvent = MessageReceivedEvent(
                participantId = UUID.randomUUID(),
                participantName = Random.nextString(8),
                type = Random.nextString(8),
                payload = Random.nextString(8),
            )
            this[messageReceivedEvent] = MessageReceivedConferenceEvent(
                at = at,
                participantId = messageReceivedEvent.participantId,
                participantName = messageReceivedEvent.participantName,
                type = messageReceivedEvent.type,
                payload = messageReceivedEvent.payload,
            )
            val referEvent = ReferEvent(
                conferenceAlias = Random.nextString(8),
                token = Random.nextString(8),
            )
            this[referEvent] = ReferConferenceEvent(
                at = at,
                conferenceAlias = referEvent.conferenceAlias,
                token = referEvent.token,
            )
            val disconnectEvent = DisconnectEvent(Random.nextString(8))
            this[disconnectEvent] = DisconnectConferenceEvent(
                at = at,
                reason = disconnectEvent.reason,
            )
            this[TestEvent] = null
            val t = Throwable()
            this[t] = FailureConferenceEvent(at, t)
        }
        testCases.forEach { (value, conferenceEvent) ->
            assertEquals(
                expected = conferenceEvent,
                actual = when (value) {
                    is Throwable -> ConferenceEvent(value) { at }
                    is Event -> ConferenceEvent(value) { at }
                    else -> fail()
                },
            )
        }
    }

    private object TestEvent : Event

    private suspend fun <T> MutableSharedFlow<T>.awaitSubscribers() {
        subscriptionCount.first { it > 0 }
    }

    private fun TestScope.testConferenceStep() = object : TestConferenceStep() {

        override fun events(token: Token): EventSourceFactory {
            assertThat(token, "token").isEqualTo(store.get())
            return TestEventSourceFactory(backgroundScope, event)
        }
    }

    private class TestEventSourceFactory(
        private val scope: CoroutineScope,
        private val event: Flow<Result<Event>>,
    ) : EventSourceFactory {

        override fun create(listener: EventSourceListener): EventSource =
            TestEventSource(scope, event, listener)
    }

    private class TestEventSource(
        scope: CoroutineScope,
        event: Flow<Result<Event>>,
        listener: EventSourceListener,
    ) : EventSource {

        private val job = event
            .onEach {
                it.fold(
                    onSuccess = { event -> listener.onEvent(this, event) },
                    onFailure = { t -> listener.onClosed(this, t) },
                )
            }
            .launchIn(scope)

        override fun cancel(): Unit = job.cancel()
    }

    private fun Assert<ConferenceEvent>.at(at: Long) = prop(ConferenceEvent::at).isEqualTo(at)
}
