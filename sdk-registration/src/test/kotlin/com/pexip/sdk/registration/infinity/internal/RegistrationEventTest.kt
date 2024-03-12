/*
 * Copyright 2022-2024 Pexip AS
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
package com.pexip.sdk.registration.infinity.internal

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
import com.pexip.sdk.api.infinity.IncomingCancelledEvent
import com.pexip.sdk.api.infinity.IncomingEvent
import com.pexip.sdk.api.infinity.InfinityService
import com.pexip.sdk.api.infinity.Token
import com.pexip.sdk.api.infinity.TokenStore
import com.pexip.sdk.registration.FailureRegistrationEvent
import com.pexip.sdk.registration.IncomingCancelledRegistrationEvent
import com.pexip.sdk.registration.IncomingRegistrationEvent
import com.pexip.sdk.registration.RegistrationEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlin.properties.Delegates
import kotlin.random.Random
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

internal class RegistrationEventTest {

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
    fun `maps Event to RegistrationEvent`() = runTest {
        val step = testRegistrationStep()
        step.registrationEvent(store) { at }.test {
            event.awaitSubscribers()
            val incomingEvent = IncomingEvent(
                conferenceAlias = Random.nextString(8),
                remoteDisplayName = Random.nextString(8),
                token = Random.nextString(8),
            )
            event.emit(Result.success(incomingEvent))
            assertThat(awaitItem(), "incomingEvent")
                .isInstanceOf<IncomingRegistrationEvent>()
                .all {
                    at(at)
                    prop(IncomingRegistrationEvent::conferenceAlias)
                        .isEqualTo(incomingEvent.conferenceAlias)
                    prop(IncomingRegistrationEvent::remoteDisplayName)
                        .isEqualTo(incomingEvent.remoteDisplayName)
                    prop(IncomingRegistrationEvent::token)
                        .isEqualTo(incomingEvent.token)
                }
            val incomingCancelledEvent = IncomingCancelledEvent(Random.nextString(8))
            event.emit(Result.success(incomingCancelledEvent))
            assertThat(awaitItem(), "incomingCancelledEvent")
                .isInstanceOf<IncomingCancelledRegistrationEvent>()
                .all {
                    at(at)
                    prop(IncomingCancelledRegistrationEvent::token)
                        .isEqualTo(incomingCancelledEvent.token)
                }
            val byeEvent = ByeEvent
            event.emit(Result.success(byeEvent))
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `failure restarts the flow`() = runTest {
        val step = testRegistrationStep()
        step.registrationEvent(store) { at }.test {
            event.awaitSubscribers()
            val incomingCancelledEvent = IncomingCancelledEvent(Random.nextString(8))
            event.emit(Result.success(incomingCancelledEvent))
            assertThat(awaitItem(), "incomingCancelledEvent")
                .isInstanceOf<IncomingCancelledRegistrationEvent>()
                .all {
                    at(at)
                    prop(IncomingCancelledRegistrationEvent::token)
                        .isEqualTo(incomingCancelledEvent.token)
                }
            event.emit(Result.failure(Throwable()))
            event.awaitSubscribers()
            event.emit(Result.success(incomingCancelledEvent))
            assertThat(awaitItem(), "presentationStopEvent")
                .isInstanceOf<IncomingCancelledRegistrationEvent>()
                .all {
                    at(at)
                    prop(IncomingCancelledRegistrationEvent::token)
                        .isEqualTo(incomingCancelledEvent.token)
                }
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `returns RegistrationEvent if type is registered`() {
        val testCases = buildMap {
            val incomingEvent = IncomingEvent(
                conferenceAlias = Random.nextString(8),
                remoteDisplayName = Random.nextString(8),
                token = Random.nextString(8),
            )
            this[incomingEvent] = IncomingRegistrationEvent(
                at = at,
                conferenceAlias = incomingEvent.conferenceAlias,
                remoteDisplayName = incomingEvent.remoteDisplayName,
                token = incomingEvent.token,
            )
            val incomingCancelledEvent = IncomingCancelledEvent(Random.nextString(8))
            this[incomingCancelledEvent] = IncomingCancelledRegistrationEvent(
                at = at,
                token = incomingCancelledEvent.token,
            )
            this[TestEvent] = null
            val t = Throwable()
            this[t] = FailureRegistrationEvent(at, t)
        }
        testCases.forEach { (value, registrationEvent) ->
            assertEquals(
                expected = registrationEvent,
                actual = when (value) {
                    is Throwable -> RegistrationEvent(value) { at }
                    is Event -> RegistrationEvent(value) { at }
                    else -> fail()
                },
            )
        }
    }

    private data object TestEvent : Event

    private suspend fun <T> MutableSharedFlow<T>.awaitSubscribers() {
        subscriptionCount.first { it > 0 }
    }

    private fun TestScope.testRegistrationStep() = object : InfinityService.RegistrationStep {

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

    private fun Assert<RegistrationEvent>.at(at: Long) = prop(RegistrationEvent::at).isEqualTo(at)
}
