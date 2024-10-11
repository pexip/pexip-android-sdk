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
package com.pexip.sdk.conference.infinity.internal

import com.pexip.sdk.api.Event
import com.pexip.sdk.api.infinity.DisconnectEvent
import com.pexip.sdk.api.infinity.ReferEvent
import com.pexip.sdk.conference.DisconnectConferenceEvent
import com.pexip.sdk.conference.FailureConferenceEvent
import com.pexip.sdk.conference.ReferConferenceEvent
import com.pexip.sdk.infinity.test.nextString
import kotlinx.datetime.Clock
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ConferenceEventTest {

    @Test
    fun `returns ConferenceEvent if type is registered`() {
        val at = Clock.System.now()
        val testCases = buildMap {
            val referEvent = ReferEvent(
                conferenceAlias = Random.nextString(),
                token = Random.nextString(),
            )
            this[referEvent] = ReferConferenceEvent(
                at = at,
                conferenceAlias = referEvent.conferenceAlias,
                token = referEvent.token,
            )
            val disconnectEvent = DisconnectEvent(Random.nextString())
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
}
