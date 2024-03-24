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

import com.pexip.sdk.api.Event
import com.pexip.sdk.api.infinity.IncomingCancelledEvent
import com.pexip.sdk.api.infinity.IncomingEvent
import com.pexip.sdk.registration.FailureRegistrationEvent
import com.pexip.sdk.registration.IncomingCancelledRegistrationEvent
import com.pexip.sdk.registration.IncomingRegistrationEvent
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

internal class RegistrationEventTest {

    @Test
    fun `returns RegistrationEvent if type is registered`() {
        val at = Random.nextLong(Long.MAX_VALUE)
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
}
