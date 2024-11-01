/*
 * Copyright 2024 Pexip AS
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
import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.key
import assertk.assertions.prop
import com.pexip.sdk.api.Event
import com.pexip.sdk.api.infinity.BreakoutBeginEvent
import com.pexip.sdk.api.infinity.BreakoutEndEvent
import com.pexip.sdk.conference.Breakout
import com.pexip.sdk.infinity.test.nextBreakoutId
import com.pexip.sdk.infinity.test.nextParticipantId
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.random.Random
import kotlin.test.BeforeTest
import kotlin.test.Test

class BreakoutsImplTest {

    private lateinit var event: MutableSharedFlow<Event>

    @BeforeTest
    fun setUp() {
        event = MutableSharedFlow(extraBufferCapacity = 1)
    }

    @Test
    fun `breakout_begin and breakout_end correctly modify the collection`() = runTest {
        val breakouts = BreakoutsImpl(backgroundScope, event)
        breakouts.breakouts.test {
            event.subscriptionCount.first { it > 0 }
            assertThat(awaitItem(), "breakouts").isEmpty()
            val breakoutBeginEvent = BreakoutBeginEvent(
                id = Random.nextBreakoutId(),
                participantId = Random.nextParticipantId(),
            )
            event.emit(breakoutBeginEvent)
            assertThat(awaitItem(), "breakouts").key(breakoutBeginEvent.id).all {
                prop(Breakout::id).isEqualTo(breakoutBeginEvent.id)
                prop(Breakout::participantId).isEqualTo(breakoutBeginEvent.participantId)
            }
            val missingBreakoutEndEvent = BreakoutEndEvent(
                id = Random.nextBreakoutId(),
                participantId = Random.nextParticipantId(),
            )
            event.emit(missingBreakoutEndEvent)
            expectNoEvents()
            val breakoutEndEvent = BreakoutEndEvent(
                id = breakoutBeginEvent.id,
                participantId = breakoutBeginEvent.participantId,
            )
            event.emit(breakoutEndEvent)
            assertThat(awaitItem(), "breakouts").isEmpty()
        }
    }
}
