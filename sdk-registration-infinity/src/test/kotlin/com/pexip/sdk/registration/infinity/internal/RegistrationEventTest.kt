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
    fun `returns ConferenceEvent if type is registered`() {
        val at = Random.nextLong(Long.MAX_VALUE)
        val testCases = buildMap {
            val incomingEvent = IncomingEvent(
                conferenceAlias = Random.nextString(8),
                remoteDisplayName = Random.nextString(8),
                token = Random.nextString(8)
            )
            this[incomingEvent] = IncomingRegistrationEvent(
                at = at,
                conferenceAlias = incomingEvent.conferenceAlias,
                remoteDisplayName = incomingEvent.remoteDisplayName,
                token = incomingEvent.token
            )
            val incomingCancelledEvent = IncomingCancelledEvent(Random.nextString(8))
            this[incomingCancelledEvent] = IncomingCancelledRegistrationEvent(
                at = at,
                token = incomingCancelledEvent.token
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
                }
            )
        }
    }

    private object TestEvent : Event
}
