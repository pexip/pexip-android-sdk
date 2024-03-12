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

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import com.pexip.sdk.api.Event
import com.pexip.sdk.api.EventSource
import com.pexip.sdk.api.EventSourceFactory
import com.pexip.sdk.api.EventSourceListener
import com.pexip.sdk.api.infinity.InfinityService
import com.pexip.sdk.api.infinity.Token
import com.pexip.sdk.api.infinity.TokenStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlin.random.Random
import kotlin.test.BeforeTest
import kotlin.test.Test

class EventsTest {

    private lateinit var event: MutableSharedFlow<Result<Event>>
    private lateinit var store: TokenStore

    @BeforeTest
    fun setUp() {
        event = MutableSharedFlow()
        store = TokenStore.create(Random.nextToken())
    }

    @Test
    fun `maps Event to ConferenceEvent`() = runTest {
        val step = testConferenceStep()
        step.events(store).test {
            event.awaitSubscriptionCountAtLeast(1)
            val events = List(10) { TestEvent() }
            events.forEach {
                event.emit(Result.success(it))
                assertThat(awaitItem(), "event").isEqualTo(it)
            }
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `failure restarts the flow`() = runTest {
        val step = testConferenceStep()
        step.events(store).test {
            event.awaitSubscriptionCountAtLeast(1)
            val event1 = TestEvent()
            event.emit(Result.success(event1))
            assertThat(awaitItem(), "event").isEqualTo(event1)
            event.emit(Result.failure(Throwable()))
            event.awaitSubscriptionCountAtLeast(1)
            val event2 = TestEvent()
            event.emit(Result.success(event2))
            assertThat(awaitItem(), "event").isEqualTo(event2)
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun TestScope.testConferenceStep() = object : InfinityService.ConferenceStep {

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

    private class TestEvent : Event
}
