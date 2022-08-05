package com.pexip.sdk.conference.infinity.internal

import com.pexip.sdk.api.Event
import com.pexip.sdk.api.infinity.DisconnectEvent
import com.pexip.sdk.api.infinity.MessageReceivedEvent
import com.pexip.sdk.api.infinity.PresentationStartEvent
import com.pexip.sdk.api.infinity.PresentationStopEvent
import com.pexip.sdk.conference.DisconnectConferenceEvent
import com.pexip.sdk.conference.MessageReceivedConferenceEvent
import com.pexip.sdk.conference.PresentationStartConferenceEvent
import com.pexip.sdk.conference.PresentationStopConferenceEvent
import java.util.UUID
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

internal class ConferenceEventTest {

    @Test
    fun `returns ConferenceEvent if type is registered`() {
        val at = Random.nextLong(Long.MAX_VALUE)
        val testCases = buildMap {
            val presentationStartEvent = PresentationStartEvent(
                presenterId = UUID.randomUUID(),
                presenterName = Random.nextString(8)
            )
            this[presentationStartEvent] = PresentationStartConferenceEvent(
                at = at,
                presenterId = presentationStartEvent.presenterId,
                presenterName = presentationStartEvent.presenterName
            )
            this[PresentationStopEvent] = PresentationStopConferenceEvent(at)
            val messageReceivedEvent = MessageReceivedEvent(
                participantId = UUID.randomUUID(),
                participantName = Random.nextString(8),
                type = Random.nextString(8),
                payload = Random.nextString(8)
            )
            this[messageReceivedEvent] = MessageReceivedConferenceEvent(
                at = at,
                participantId = messageReceivedEvent.participantId,
                participantName = messageReceivedEvent.participantName,
                type = messageReceivedEvent.type,
                payload = messageReceivedEvent.payload
            )
            val disconnectEvent = DisconnectEvent(Random.nextString(8))
            this[disconnectEvent] = DisconnectConferenceEvent(
                at = at,
                reason = disconnectEvent.reason
            )
            this[TestEvent] = null
        }
        testCases.forEach { (event, conferenceEvent) ->
            assertEquals(conferenceEvent, ConferenceEvent(event) { at })
        }
    }

    private object TestEvent : Event
}
