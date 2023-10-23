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

import com.pexip.sdk.api.Event
import com.pexip.sdk.api.infinity.DisconnectEvent
import com.pexip.sdk.api.infinity.MessageReceivedEvent
import com.pexip.sdk.api.infinity.PresentationStartEvent
import com.pexip.sdk.api.infinity.PresentationStopEvent
import com.pexip.sdk.api.infinity.ReferEvent
import com.pexip.sdk.api.infinity.TokenStore
import com.pexip.sdk.conference.DisconnectConferenceEvent
import com.pexip.sdk.conference.FailureConferenceEvent
import com.pexip.sdk.conference.MessageReceivedConferenceEvent
import com.pexip.sdk.conference.PresentationStartConferenceEvent
import com.pexip.sdk.conference.PresentationStopConferenceEvent
import com.pexip.sdk.conference.ReferConferenceEvent
import kotlinx.coroutines.flow.MutableSharedFlow
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
}
