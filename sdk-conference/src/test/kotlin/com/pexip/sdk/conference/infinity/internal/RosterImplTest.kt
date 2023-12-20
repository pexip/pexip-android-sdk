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
import assertk.assertThat
import assertk.assertions.containsOnly
import assertk.assertions.doesNotContain
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import com.pexip.sdk.api.Event
import com.pexip.sdk.api.infinity.ParticipantCreateEvent
import com.pexip.sdk.api.infinity.ParticipantDeleteEvent
import com.pexip.sdk.api.infinity.ParticipantResponse
import com.pexip.sdk.api.infinity.ParticipantSyncBeginEvent
import com.pexip.sdk.api.infinity.ParticipantSyncEndEvent
import com.pexip.sdk.api.infinity.ParticipantUpdateEvent
import com.pexip.sdk.conference.Participant
import com.pexip.sdk.conference.Role
import com.pexip.sdk.conference.ServiceType
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import java.util.UUID
import kotlin.random.Random
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds
import com.pexip.sdk.api.infinity.Role as ApiRole
import com.pexip.sdk.api.infinity.ServiceType as ApiServiceType

class RosterImplTest {

    private lateinit var event: MutableSharedFlow<Event>

    @BeforeTest
    fun setUp() {
        event = MutableSharedFlow(extraBufferCapacity = 1)
    }

    @Test
    fun `does not update the list until syncing is finished`() = runTest {
        val roster = RosterImpl(backgroundScope, event)
        roster.participants.test {
            event.subscriptionCount.first { it > 0 }
            assertThat(awaitItem(), "participants").isEmpty()
            event.emit(ParticipantSyncBeginEvent)
            val participants = List(100) { Random.nextParticipant(it.toLong()) }
            participants.forEach {
                val response = it.toParticipantResponse()
                event.emit(ParticipantCreateEvent(response))
            }
            expectNoEvents()
            event.emit(ParticipantSyncEndEvent)
            assertThat(awaitItem(), "participants").isEqualTo(participants)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `create, update, delete correctly modify the list`() = runTest {
        val roster = RosterImpl(backgroundScope, event)
        roster.participants.test {
            event.subscriptionCount.first { it > 0 }
            assertThat(awaitItem(), "participants").isEmpty()
            var participant = Random.nextParticipant()
            var response = participant.toParticipantResponse()
            var e: Event = ParticipantCreateEvent(response)
            event.emit(e)
            assertThat(awaitItem(), "participants").containsOnly(participant)
            participant = Random.nextParticipant(id = participant.id)
            response = participant.toParticipantResponse()
            e = ParticipantUpdateEvent(response)
            event.emit(e)
            assertThat(awaitItem(), "participants").containsOnly(participant)
            e = ParticipantDeleteEvent(participant.id)
            event.emit(e)
            assertThat(awaitItem(), "participants").doesNotContain(participant)
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `update without a matching create does not modify the list`() = runTest {
        val roster = RosterImpl(backgroundScope, event)
        roster.participants.test {
            event.subscriptionCount.first { it > 0 }
            assertThat(awaitItem(), "participants").isEmpty()
            val participant = Random.nextParticipant()
            val response = participant.toParticipantResponse()
            val e = ParticipantUpdateEvent(response)
            event.emit(e)
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun Random.nextParticipant(index: Long = 0, id: UUID = UUID.randomUUID()): Participant {
        val startTime = Instant.fromEpochSeconds(index)
        return Participant(
            id = id,
            startTime = startTime,
            role = Role.entries.random(this),
            serviceType = ServiceType.entries.random(this),
            buzzTime = startTime + nextInt(0, 100).seconds,
            spotlightTime = startTime + nextInt(0, 100).seconds,
            displayName = nextString(8),
            overlayText = nextString(8),
            audioMuted = nextBoolean(),
            videoMuted = nextBoolean(),
            presenting = nextBoolean(),
            muteSupported = nextBoolean(),
            transferSupported = nextBoolean(),
            disconnectSupported = nextBoolean(),
        )
    }

    private fun Participant.toParticipantResponse() = ParticipantResponse(
        id = id,
        startTime = startTime,
        buzzTime = buzzTime,
        spotlightTime = spotlightTime,
        displayName = displayName,
        overlayText = overlayText,
        presenting = presenting,
        audioMuted = audioMuted,
        videoMuted = videoMuted,
        muteSupported = muteSupported,
        transferSupported = transferSupported,
        disconnectSupported = disconnectSupported,
        role = when (role) {
            Role.HOST -> ApiRole.HOST
            Role.GUEST -> ApiRole.GUEST
            Role.UNKNOWN -> ApiRole.UNKNOWN
        },
        serviceType = when (serviceType) {
            ServiceType.CONNECTING -> ApiServiceType.CONNECTING
            ServiceType.WAITING_ROOM -> ApiServiceType.WAITING_ROOM
            ServiceType.IVR -> ApiServiceType.IVR
            ServiceType.CONFERENCE -> ApiServiceType.CONFERENCE
            ServiceType.LECTURE -> ApiServiceType.LECTURE
            ServiceType.GATEWAY -> ApiServiceType.GATEWAY
            ServiceType.TEST_CALL -> ApiServiceType.TEST_CALL
            ServiceType.UNKNOWN -> ApiServiceType.UNKNOWN
        },
    )
}
