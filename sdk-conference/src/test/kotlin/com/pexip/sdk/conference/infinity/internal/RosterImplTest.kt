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
import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.containsOnly
import assertk.assertions.doesNotContain
import assertk.assertions.hasCause
import assertk.assertions.index
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isIn
import assertk.assertions.isInstanceOf
import assertk.assertions.isNull
import assertk.assertions.isTrue
import com.pexip.sdk.api.Call
import com.pexip.sdk.api.Callback
import com.pexip.sdk.api.Event
import com.pexip.sdk.api.infinity.ConferenceUpdateEvent
import com.pexip.sdk.api.infinity.InfinityService
import com.pexip.sdk.api.infinity.ParticipantCreateEvent
import com.pexip.sdk.api.infinity.ParticipantDeleteEvent
import com.pexip.sdk.api.infinity.ParticipantResponse
import com.pexip.sdk.api.infinity.ParticipantSyncBeginEvent
import com.pexip.sdk.api.infinity.ParticipantSyncEndEvent
import com.pexip.sdk.api.infinity.ParticipantUpdateEvent
import com.pexip.sdk.api.infinity.RoleRequest
import com.pexip.sdk.api.infinity.SpeakerResponse
import com.pexip.sdk.api.infinity.StageEvent
import com.pexip.sdk.api.infinity.Token
import com.pexip.sdk.api.infinity.TokenStore
import com.pexip.sdk.conference.AdmitException
import com.pexip.sdk.conference.DisconnectAllException
import com.pexip.sdk.conference.DisconnectException
import com.pexip.sdk.conference.LockException
import com.pexip.sdk.conference.LowerAllHandsException
import com.pexip.sdk.conference.LowerHandException
import com.pexip.sdk.conference.MakeGuestException
import com.pexip.sdk.conference.MakeHostException
import com.pexip.sdk.conference.MuteAllGuestsException
import com.pexip.sdk.conference.MuteException
import com.pexip.sdk.conference.Participant
import com.pexip.sdk.conference.RaiseHandException
import com.pexip.sdk.conference.Role
import com.pexip.sdk.conference.ServiceType
import com.pexip.sdk.conference.SpotlightException
import com.pexip.sdk.conference.UnlockException
import com.pexip.sdk.conference.UnmuteAllGuestsException
import com.pexip.sdk.conference.UnmuteException
import com.pexip.sdk.conference.UnspotlightException
import com.pexip.sdk.infinity.ParticipantId
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import kotlin.properties.Delegates
import kotlin.random.Random
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds
import com.pexip.sdk.api.infinity.Role as ApiRole
import com.pexip.sdk.api.infinity.ServiceType as ApiServiceType

class RosterImplTest {

    private lateinit var event: MutableSharedFlow<Event>
    private lateinit var store: TokenStore

    private var participantId: ParticipantId by Delegates.notNull()

    @BeforeTest
    fun setUp() {
        event = MutableSharedFlow(extraBufferCapacity = 1)
        participantId = ParticipantId(Random.nextString(8))
        store = TokenStore.create(Random.nextToken())
    }

    @Test
    fun `does not update the list until syncing is finished`() = runTest {
        val roster = RosterImpl(
            scope = backgroundScope,
            event = event,
            participantId = participantId,
            store = store,
            step = object : InfinityService.ConferenceStep {},
        )
        roster.participants.test {
            event.subscriptionCount.first { it > 0 }
            assertThat(awaitItem(), "participants").isEmpty()
            event.emit(ParticipantSyncBeginEvent)
            val participants = List(100) { Random.nextParticipant(it) }
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
    fun `create, update, delete, stage correctly modify the list`() = runTest {
        val roster = RosterImpl(
            scope = backgroundScope,
            event = event,
            participantId = participantId,
            store = store,
            step = object : InfinityService.ConferenceStep {},
        )
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
            repeat(10) {
                participant = participant.copy(speaking = !participant.speaking)
                e = StageEvent(
                    SpeakerResponse(
                        participantId = participant.id,
                        vad = if (participant.speaking) 100 else 0,
                    ),
                )
                event.emit(e)
                assertThat(awaitItem(), "participants").containsOnly(participant)
            }
            e = ParticipantDeleteEvent(participant.id)
            event.emit(e)
            assertThat(awaitItem(), "participants").doesNotContain(participant)
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `update without a matching create does not modify the list`() = runTest {
        val roster = RosterImpl(
            scope = backgroundScope,
            event = event,
            participantId = participantId,
            store = store,
            step = object : InfinityService.ConferenceStep {},
        )
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

    @Test
    fun `me produces the correct participant`() = runTest {
        val roster = RosterImpl(
            scope = backgroundScope,
            event = event,
            participantId = participantId,
            store = store,
            step = object : InfinityService.ConferenceStep {},
        )
        roster.me.test {
            event.subscriptionCount.first { it > 0 }
            assertThat(awaitItem(), "me").isNull()
            var me = Random.nextParticipant(id = participantId)
            event.emit(ParticipantCreateEvent(me.toParticipantResponse()))
            assertThat(awaitItem(), "me").isEqualTo(me)
            me = me.copy(audioMuted = !me.audioMuted)
            event.emit(ParticipantUpdateEvent(me.toParticipantResponse()))
            assertThat(awaitItem(), "me").isEqualTo(me)
            // No update with the same Participant
            event.emit(ParticipantUpdateEvent(me.toParticipantResponse()))
            // No update with another Participant
            event.emit(ParticipantCreateEvent(Random.nextParticipant().toParticipantResponse()))
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `locked produces the correct lock state`() = runTest {
        val roster = RosterImpl(
            scope = backgroundScope,
            event = event,
            participantId = participantId,
            store = store,
            step = object : InfinityService.ConferenceStep {},
        )
        roster.locked.test {
            event.subscriptionCount.first { it > 0 }
            assertThat(awaitItem(), "locked").isFalse()
            event.emit(ConferenceUpdateEvent(locked = true))
            assertThat(awaitItem(), "locked").isTrue()
            event.emit(ConferenceUpdateEvent(locked = true))
            expectNoEvents()
            event.emit(ConferenceUpdateEvent(locked = false))
            assertThat(awaitItem(), "locked").isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `allGuestsMuted produces the correct all guests muted state`() = runTest {
        val roster = RosterImpl(
            scope = backgroundScope,
            event = event,
            participantId = participantId,
            store = store,
            step = object : InfinityService.ConferenceStep {},
        )
        roster.allGuestsMuted.test {
            event.subscriptionCount.first { it > 0 }
            assertThat(awaitItem(), "guestsMuted").isFalse()
            event.emit(ConferenceUpdateEvent(guestsMuted = true))
            assertThat(awaitItem(), "guestsMuted").isTrue()
            event.emit(ConferenceUpdateEvent(guestsMuted = true))
            expectNoEvents()
            event.emit(ConferenceUpdateEvent(guestsMuted = false))
            assertThat(awaitItem(), "guestsMuted").isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `raiseHand() returns if participantId does not exist`() = runTest {
        val roster = RosterImpl(
            scope = backgroundScope,
            event = event,
            participantId = participantId,
            store = store,
            step = object : InfinityService.ConferenceStep {},
        )
        roster.raiseHand(ParticipantId(Random.nextString(8)))
    }

    @Test
    fun `raiseHand() throws RaiseHandException`() = runTest {
        val participants = List(10) { Random.nextParticipant(it) }
        val causes = participants.associate { it.id to Throwable() }
        val roster = RosterImpl(
            scope = backgroundScope,
            event = event,
            participantId = participantId,
            store = store,
            step = object : InfinityService.ConferenceStep {

                override fun participant(participantId: ParticipantId): InfinityService.ParticipantStep {
                    assertThat(participantId, "participantId")
                        .isIn(*participants.map(Participant::id).toTypedArray())
                    return object : InfinityService.ParticipantStep {

                        override fun buzz(token: Token): Call<Boolean> {
                            assertThat(token, "token").isEqualTo(store.get())
                            return object : TestCall<Boolean> {

                                override fun enqueue(callback: Callback<Boolean>) =
                                    callback.onFailure(this, causes.getValue(participantId))
                            }
                        }
                    }
                }
            },
        )
        roster.participants.test {
            event.subscriptionCount.first { it > 0 }
            assertThat(awaitItem(), "participants").isEmpty()
            participants.forEachIndexed { index, participant ->
                val response = participant.toParticipantResponse()
                val e = ParticipantCreateEvent(response)
                event.emit(e)
                assertThat(awaitItem(), "participants")
                    .index(index)
                    .isEqualTo(participant)
            }
        }
        participants.forEach {
            assertFailure { roster.raiseHand(it.id) }
                .isInstanceOf<RaiseHandException>()
                .hasCause(causes.getValue(it.id))
        }
    }

    @Test
    fun `raiseHand() returns`() = runTest {
        val participants = List(10) { Random.nextParticipant(it) }
        val roster = RosterImpl(
            scope = backgroundScope,
            event = event,
            participantId = participantId,
            store = store,
            step = object : InfinityService.ConferenceStep {

                override fun participant(participantId: ParticipantId): InfinityService.ParticipantStep {
                    assertThat(participantId, "participantId")
                        .isIn(*participants.map(Participant::id).toTypedArray())
                    return object : InfinityService.ParticipantStep {

                        override fun buzz(token: Token): Call<Boolean> {
                            assertThat(token, "token").isEqualTo(store.get())
                            return object : TestCall<Boolean> {

                                override fun enqueue(callback: Callback<Boolean>) =
                                    callback.onSuccess(this, true)
                            }
                        }
                    }
                }
            },
        )
        roster.participants.test {
            event.subscriptionCount.first { it > 0 }
            assertThat(awaitItem(), "participants").isEmpty()
            participants.forEachIndexed { index, participant ->
                val response = participant.toParticipantResponse()
                val e = ParticipantCreateEvent(response)
                event.emit(e)
                assertThat(awaitItem(), "participants")
                    .index(index)
                    .isEqualTo(participant)
            }
        }
        participants.forEach { roster.raiseHand(it.id) }
    }

    @Test
    fun `admit() returns if participantId does not exist`() = runTest {
        val roster = RosterImpl(
            scope = backgroundScope,
            event = event,
            participantId = participantId,
            store = store,
            step = object : InfinityService.ConferenceStep {},
        )
        roster.disconnect(ParticipantId(Random.nextString(8)))
    }

    @Test
    fun `admit() throws AdmitException`() = runTest {
        val participants = List(10) { Random.nextParticipant(it) }
        val causes = participants.associate { it.id to Throwable() }
        val roster = RosterImpl(
            scope = backgroundScope,
            event = event,
            participantId = participantId,
            store = store,
            step = object : InfinityService.ConferenceStep {

                override fun participant(participantId: ParticipantId): InfinityService.ParticipantStep {
                    assertThat(participantId, "participantId")
                        .isIn(*participants.map(Participant::id).toTypedArray())
                    return object : InfinityService.ParticipantStep {

                        override fun unlock(token: Token): Call<Boolean> {
                            assertThat(token, "token").isEqualTo(store.get())
                            return object : TestCall<Boolean> {

                                override fun enqueue(callback: Callback<Boolean>) =
                                    callback.onFailure(this, causes.getValue(participantId))
                            }
                        }
                    }
                }
            },
        )
        roster.participants.test {
            event.subscriptionCount.first { it > 0 }
            assertThat(awaitItem(), "participants").isEmpty()
            participants.forEachIndexed { index, participant ->
                val response = participant.toParticipantResponse()
                val e = ParticipantCreateEvent(response)
                event.emit(e)
                assertThat(awaitItem(), "participants")
                    .index(index)
                    .isEqualTo(participant)
            }
        }
        participants.forEach {
            assertFailure { roster.admit(it.id) }
                .isInstanceOf<AdmitException>()
                .hasCause(causes.getValue(it.id))
        }
    }

    @Test
    fun `admit() returns`() = runTest {
        val participants = List(10) { Random.nextParticipant(it) }
        val roster = RosterImpl(
            scope = backgroundScope,
            event = event,
            participantId = participantId,
            store = store,
            step = object : InfinityService.ConferenceStep {

                override fun participant(participantId: ParticipantId): InfinityService.ParticipantStep {
                    assertThat(participantId, "participantId")
                        .isIn(*participants.map(Participant::id).toTypedArray())
                    return object : InfinityService.ParticipantStep {

                        override fun unlock(token: Token): Call<Boolean> {
                            assertThat(token, "token").isEqualTo(store.get())
                            return object : TestCall<Boolean> {

                                override fun enqueue(callback: Callback<Boolean>) =
                                    callback.onSuccess(this, true)
                            }
                        }
                    }
                }
            },
        )
        roster.participants.test {
            event.subscriptionCount.first { it > 0 }
            assertThat(awaitItem(), "participants").isEmpty()
            participants.forEachIndexed { index, participant ->
                val response = participant.toParticipantResponse()
                val e = ParticipantCreateEvent(response)
                event.emit(e)
                assertThat(awaitItem(), "participants")
                    .index(index)
                    .isEqualTo(participant)
            }
        }
        participants.forEach { roster.admit(it.id) }
    }

    @Test
    fun `disconnect() returns if participantId does not exist`() = runTest {
        val roster = RosterImpl(
            scope = backgroundScope,
            event = event,
            participantId = participantId,
            store = store,
            step = object : InfinityService.ConferenceStep {},
        )
        roster.disconnect(ParticipantId(Random.nextString(8)))
    }

    @Test
    fun `disconnect() throws DisconnectException`() = runTest {
        val participants = List(10) { Random.nextParticipant(it) }
        val causes = participants.associate { it.id to Throwable() }
        val roster = RosterImpl(
            scope = backgroundScope,
            event = event,
            participantId = participantId,
            store = store,
            step = object : InfinityService.ConferenceStep {

                override fun participant(participantId: ParticipantId): InfinityService.ParticipantStep {
                    assertThat(participantId, "participantId")
                        .isIn(*participants.map(Participant::id).toTypedArray())
                    return object : InfinityService.ParticipantStep {

                        override fun disconnect(token: Token): Call<Boolean> {
                            assertThat(token, "token").isEqualTo(store.get())
                            return object : TestCall<Boolean> {

                                override fun enqueue(callback: Callback<Boolean>) =
                                    callback.onFailure(this, causes.getValue(participantId))
                            }
                        }
                    }
                }
            },
        )
        roster.participants.test {
            event.subscriptionCount.first { it > 0 }
            assertThat(awaitItem(), "participants").isEmpty()
            participants.forEachIndexed { index, participant ->
                val response = participant.toParticipantResponse()
                val e = ParticipantCreateEvent(response)
                event.emit(e)
                assertThat(awaitItem(), "participants")
                    .index(index)
                    .isEqualTo(participant)
            }
        }
        participants.forEach {
            assertFailure { roster.disconnect(it.id) }
                .isInstanceOf<DisconnectException>()
                .hasCause(causes.getValue(it.id))
        }
    }

    @Test
    fun `disconnect() returns`() = runTest {
        val participants = List(10) { Random.nextParticipant(it) }
        val roster = RosterImpl(
            scope = backgroundScope,
            event = event,
            participantId = participantId,
            store = store,
            step = object : InfinityService.ConferenceStep {

                override fun participant(participantId: ParticipantId): InfinityService.ParticipantStep {
                    assertThat(participantId, "participantId")
                        .isIn(*participants.map(Participant::id).toTypedArray())
                    return object : InfinityService.ParticipantStep {

                        override fun disconnect(token: Token): Call<Boolean> {
                            assertThat(token, "token").isEqualTo(store.get())
                            return object : TestCall<Boolean> {

                                override fun enqueue(callback: Callback<Boolean>) =
                                    callback.onSuccess(this, true)
                            }
                        }
                    }
                }
            },
        )
        roster.participants.test {
            event.subscriptionCount.first { it > 0 }
            assertThat(awaitItem(), "participants").isEmpty()
            participants.forEachIndexed { index, participant ->
                val response = participant.toParticipantResponse()
                val e = ParticipantCreateEvent(response)
                event.emit(e)
                assertThat(awaitItem(), "participants")
                    .index(index)
                    .isEqualTo(participant)
            }
        }
        participants.forEach { roster.disconnect(it.id) }
    }

    @Test
    fun `makeHost() throws MakeHostException`() = runTest {
        val participants = List(10) { Random.nextParticipant(it) }
        val causes = participants.associate { it.id to Throwable() }
        val roster = RosterImpl(
            scope = backgroundScope,
            event = event,
            participantId = participantId,
            store = store,
            step = object : InfinityService.ConferenceStep {

                override fun participant(participantId: ParticipantId): InfinityService.ParticipantStep {
                    assertThat(participantId, "participantId")
                        .isIn(*participants.map(Participant::id).toTypedArray())
                    return object : InfinityService.ParticipantStep {

                        override fun role(request: RoleRequest, token: Token): Call<Boolean> {
                            assertThat(token, "token").isEqualTo(store.get())
                            assertThat(request::role, "request").isEqualTo(ApiRole.HOST)
                            return object : TestCall<Boolean> {

                                override fun enqueue(callback: Callback<Boolean>) =
                                    callback.onFailure(this, causes.getValue(participantId))
                            }
                        }
                    }
                }
            },
        )
        roster.participants.test {
            event.subscriptionCount.first { it > 0 }
            assertThat(awaitItem(), "participants").isEmpty()
            participants.forEachIndexed { index, participant ->
                val response = participant.toParticipantResponse()
                val e = ParticipantCreateEvent(response)
                event.emit(e)
                assertThat(awaitItem(), "participants")
                    .index(index)
                    .isEqualTo(participant)
            }
        }
        participants.forEach {
            assertFailure { roster.makeHost(it.id) }
                .isInstanceOf<MakeHostException>()
                .hasCause(causes.getValue(it.id))
        }
    }

    @Test
    fun `makeHost() returns`() = runTest {
        val participants = List(10) { Random.nextParticipant(it) }
        val roster = RosterImpl(
            scope = backgroundScope,
            event = event,
            participantId = participantId,
            store = store,
            step = object : InfinityService.ConferenceStep {

                override fun participant(participantId: ParticipantId): InfinityService.ParticipantStep {
                    assertThat(participantId, "participantId")
                        .isIn(*participants.map(Participant::id).toTypedArray())
                    return object : InfinityService.ParticipantStep {

                        override fun role(request: RoleRequest, token: Token): Call<Boolean> {
                            assertThat(token, "token").isEqualTo(store.get())
                            assertThat(request::role).isEqualTo(ApiRole.HOST)
                            return object : TestCall<Boolean> {

                                override fun enqueue(callback: Callback<Boolean>) =
                                    callback.onSuccess(this, true)
                            }
                        }
                    }
                }
            },
        )
        roster.participants.test {
            event.subscriptionCount.first { it > 0 }
            assertThat(awaitItem(), "participants").isEmpty()
            participants.forEachIndexed { index, participant ->
                val response = participant.toParticipantResponse()
                val e = ParticipantCreateEvent(response)
                event.emit(e)
                assertThat(awaitItem(), "participants")
                    .index(index)
                    .isEqualTo(participant)
            }
        }
        participants.forEach { roster.makeHost(it.id) }
    }

    @Test
    fun `makeGuest() throws MakeGuestException`() = runTest {
        val participants = List(10) { Random.nextParticipant(it) }
        val causes = participants.associate { it.id to Throwable() }
        val roster = RosterImpl(
            scope = backgroundScope,
            event = event,
            participantId = participantId,
            store = store,
            step = object : InfinityService.ConferenceStep {

                override fun participant(participantId: ParticipantId): InfinityService.ParticipantStep {
                    assertThat(participantId, "participantId")
                        .isIn(*participants.map(Participant::id).toTypedArray())
                    return object : InfinityService.ParticipantStep {

                        override fun role(request: RoleRequest, token: Token): Call<Boolean> {
                            assertThat(token, "token").isEqualTo(store.get())
                            assertThat(request::role, "request").isEqualTo(ApiRole.GUEST)
                            return object : TestCall<Boolean> {

                                override fun enqueue(callback: Callback<Boolean>) =
                                    callback.onFailure(this, causes.getValue(participantId))
                            }
                        }
                    }
                }
            },
        )
        roster.participants.test {
            event.subscriptionCount.first { it > 0 }
            assertThat(awaitItem(), "participants").isEmpty()
            participants.forEachIndexed { index, participant ->
                val response = participant.toParticipantResponse()
                val e = ParticipantCreateEvent(response)
                event.emit(e)
                assertThat(awaitItem(), "participants")
                    .index(index)
                    .isEqualTo(participant)
            }
        }
        participants.forEach {
            assertFailure { roster.makeGuest(it.id) }
                .isInstanceOf<MakeGuestException>()
                .hasCause(causes.getValue(it.id))
        }
    }

    @Test
    fun `makeGuest() returns`() = runTest {
        val participants = List(10) { Random.nextParticipant(it) }
        val roster = RosterImpl(
            scope = backgroundScope,
            event = event,
            participantId = participantId,
            store = store,
            step = object : InfinityService.ConferenceStep {

                override fun participant(participantId: ParticipantId): InfinityService.ParticipantStep {
                    assertThat(participantId, "participantId")
                        .isIn(*participants.map(Participant::id).toTypedArray())
                    return object : InfinityService.ParticipantStep {

                        override fun role(request: RoleRequest, token: Token): Call<Boolean> {
                            assertThat(token, "token").isEqualTo(store.get())
                            assertThat(request::role).isEqualTo(ApiRole.GUEST)
                            return object : TestCall<Boolean> {

                                override fun enqueue(callback: Callback<Boolean>) =
                                    callback.onSuccess(this, true)
                            }
                        }
                    }
                }
            },
        )
        roster.participants.test {
            event.subscriptionCount.first { it > 0 }
            assertThat(awaitItem(), "participants").isEmpty()
            participants.forEachIndexed { index, participant ->
                val response = participant.toParticipantResponse()
                val e = ParticipantCreateEvent(response)
                event.emit(e)
                assertThat(awaitItem(), "participants")
                    .index(index)
                    .isEqualTo(participant)
            }
        }
        participants.forEach { roster.makeGuest(it.id) }
    }

    @Test
    fun `mute() returns if participantId does not exist`() = runTest {
        val roster = RosterImpl(
            scope = backgroundScope,
            event = event,
            participantId = participantId,
            store = store,
            step = object : InfinityService.ConferenceStep {},
        )
        roster.mute(ParticipantId(Random.nextString(8)))
    }

    @Test
    fun `mute() throws MuteException`() = runTest {
        val participants = List(10) { Random.nextParticipant(it) }
        val causes = participants.associate { it.id to Throwable() }
        val roster = RosterImpl(
            scope = backgroundScope,
            event = event,
            participantId = participantId,
            store = store,
            step = object : InfinityService.ConferenceStep {

                override fun participant(participantId: ParticipantId): InfinityService.ParticipantStep {
                    assertThat(participantId, "participantId")
                        .isIn(*participants.map(Participant::id).toTypedArray())
                    return object : InfinityService.ParticipantStep {

                        override fun mute(token: Token): Call<Unit> {
                            assertThat(token, "token").isEqualTo(store.get())
                            return object : TestCall<Unit> {

                                override fun enqueue(callback: Callback<Unit>) =
                                    callback.onFailure(this, causes.getValue(participantId))
                            }
                        }
                    }
                }
            },
        )
        roster.participants.test {
            event.subscriptionCount.first { it > 0 }
            assertThat(awaitItem(), "participants").isEmpty()
            participants.forEachIndexed { index, participant ->
                val response = participant.toParticipantResponse()
                val e = ParticipantCreateEvent(response)
                event.emit(e)
                assertThat(awaitItem(), "participants")
                    .index(index)
                    .isEqualTo(participant)
            }
        }
        participants.forEach {
            assertFailure { roster.mute(it.id) }
                .isInstanceOf<MuteException>()
                .hasCause(causes.getValue(it.id))
        }
    }

    @Test
    fun `mute() returns`() = runTest {
        val participants = List(10) { Random.nextParticipant(it) }
        val roster = RosterImpl(
            scope = backgroundScope,
            event = event,
            participantId = participantId,
            store = store,
            step = object : InfinityService.ConferenceStep {

                override fun participant(participantId: ParticipantId): InfinityService.ParticipantStep {
                    assertThat(participantId, "participantId")
                        .isIn(*participants.map(Participant::id).toTypedArray())
                    return object : InfinityService.ParticipantStep {

                        override fun mute(token: Token): Call<Unit> {
                            assertThat(token, "token").isEqualTo(store.get())
                            return object : TestCall<Unit> {

                                override fun enqueue(callback: Callback<Unit>) =
                                    callback.onSuccess(this, Unit)
                            }
                        }
                    }
                }
            },
        )
        roster.participants.test {
            event.subscriptionCount.first { it > 0 }
            assertThat(awaitItem(), "participants").isEmpty()
            participants.forEachIndexed { index, participant ->
                val response = participant.toParticipantResponse()
                val e = ParticipantCreateEvent(response)
                event.emit(e)
                assertThat(awaitItem(), "participants")
                    .index(index)
                    .isEqualTo(participant)
            }
        }
        participants.forEach { roster.mute(it.id) }
    }

    @Test
    fun `unmute() returns if participantId does not exist`() = runTest {
        val roster = RosterImpl(
            scope = backgroundScope,
            event = event,
            participantId = participantId,
            store = store,
            step = object : InfinityService.ConferenceStep {},
        )
        roster.unmute(ParticipantId(Random.nextString(8)))
    }

    @Test
    fun `unmute() throws MuteException`() = runTest {
        val participants = List(10) { Random.nextParticipant(it) }
        val causes = participants.associate { it.id to Throwable() }
        val roster = RosterImpl(
            scope = backgroundScope,
            event = event,
            participantId = participantId,
            store = store,
            step = object : InfinityService.ConferenceStep {

                override fun participant(participantId: ParticipantId): InfinityService.ParticipantStep {
                    assertThat(participantId, "participantId")
                        .isIn(*participants.map(Participant::id).toTypedArray())
                    return object : InfinityService.ParticipantStep {

                        override fun unmute(token: Token): Call<Unit> {
                            assertThat(token, "token").isEqualTo(store.get())
                            return object : TestCall<Unit> {

                                override fun enqueue(callback: Callback<Unit>) =
                                    callback.onFailure(this, causes.getValue(participantId))
                            }
                        }
                    }
                }
            },
        )
        roster.participants.test {
            event.subscriptionCount.first { it > 0 }
            assertThat(awaitItem(), "participants").isEmpty()
            participants.forEachIndexed { index, participant ->
                val response = participant.toParticipantResponse()
                val e = ParticipantCreateEvent(response)
                event.emit(e)
                assertThat(awaitItem(), "participants")
                    .index(index)
                    .isEqualTo(participant)
            }
        }
        participants.forEach {
            assertFailure { roster.unmute(it.id) }
                .isInstanceOf<UnmuteException>()
                .hasCause(causes.getValue(it.id))
        }
    }

    @Test
    fun `unmute() returns`() = runTest {
        val participants = List(10) { Random.nextParticipant(it) }
        val roster = RosterImpl(
            scope = backgroundScope,
            event = event,
            participantId = participantId,
            store = store,
            step = object : InfinityService.ConferenceStep {

                override fun participant(participantId: ParticipantId): InfinityService.ParticipantStep {
                    assertThat(participantId, "participantId")
                        .isIn(*participants.map(Participant::id).toTypedArray())
                    return object : InfinityService.ParticipantStep {

                        override fun unmute(token: Token): Call<Unit> {
                            assertThat(token, "token").isEqualTo(store.get())
                            return object : TestCall<Unit> {

                                override fun enqueue(callback: Callback<Unit>) =
                                    callback.onSuccess(this, Unit)
                            }
                        }
                    }
                }
            },
        )
        roster.participants.test {
            event.subscriptionCount.first { it > 0 }
            assertThat(awaitItem(), "participants").isEmpty()
            participants.forEachIndexed { index, participant ->
                val response = participant.toParticipantResponse()
                val e = ParticipantCreateEvent(response)
                event.emit(e)
                assertThat(awaitItem(), "participants")
                    .index(index)
                    .isEqualTo(participant)
            }
        }
        participants.forEach { roster.unmute(it.id) }
    }

    @Test
    fun `spotlight() returns if participantId does not exist`() = runTest {
        val roster = RosterImpl(
            scope = backgroundScope,
            event = event,
            participantId = participantId,
            store = store,
            step = object : InfinityService.ConferenceStep {},
        )
        roster.spotlight(ParticipantId(Random.nextString(8)))
    }

    @Test
    fun `spotlight() throws SpotlightException`() = runTest {
        val participants = List(10) { Random.nextParticipant(it) }
        val causes = participants.associate { it.id to Throwable() }
        val roster = RosterImpl(
            scope = backgroundScope,
            event = event,
            participantId = participantId,
            store = store,
            step = object : InfinityService.ConferenceStep {

                override fun participant(participantId: ParticipantId): InfinityService.ParticipantStep {
                    assertThat(participantId, "participantId")
                        .isIn(*participants.map(Participant::id).toTypedArray())
                    return object : InfinityService.ParticipantStep {

                        override fun spotlightOn(token: Token): Call<Boolean> {
                            assertThat(token, "token").isEqualTo(store.get())
                            return object : TestCall<Boolean> {

                                override fun enqueue(callback: Callback<Boolean>) =
                                    callback.onFailure(this, causes.getValue(participantId))
                            }
                        }
                    }
                }
            },
        )
        roster.participants.test {
            event.subscriptionCount.first { it > 0 }
            assertThat(awaitItem(), "participants").isEmpty()
            participants.forEachIndexed { index, participant ->
                val response = participant.toParticipantResponse()
                val e = ParticipantCreateEvent(response)
                event.emit(e)
                assertThat(awaitItem(), "participants")
                    .index(index)
                    .isEqualTo(participant)
            }
        }
        participants.forEach {
            assertFailure { roster.spotlight(it.id) }
                .isInstanceOf<SpotlightException>()
                .hasCause(causes.getValue(it.id))
        }
    }

    @Test
    fun `spotlight() returns`() = runTest {
        val participants = List(10) { Random.nextParticipant(it) }
        val roster = RosterImpl(
            scope = backgroundScope,
            event = event,
            participantId = participantId,
            store = store,
            step = object : InfinityService.ConferenceStep {

                override fun participant(participantId: ParticipantId): InfinityService.ParticipantStep {
                    assertThat(participantId, "participantId")
                        .isIn(*participants.map(Participant::id).toTypedArray())
                    return object : InfinityService.ParticipantStep {

                        override fun spotlightOn(token: Token): Call<Boolean> {
                            assertThat(token, "token").isEqualTo(store.get())
                            return object : TestCall<Boolean> {

                                override fun enqueue(callback: Callback<Boolean>) =
                                    callback.onSuccess(this, true)
                            }
                        }
                    }
                }
            },
        )
        roster.participants.test {
            event.subscriptionCount.first { it > 0 }
            assertThat(awaitItem(), "participants").isEmpty()
            participants.forEachIndexed { index, participant ->
                val response = participant.toParticipantResponse()
                val e = ParticipantCreateEvent(response)
                event.emit(e)
                assertThat(awaitItem(), "participants")
                    .index(index)
                    .isEqualTo(participant)
            }
        }
        participants.forEach { roster.spotlight(it.id) }
    }

    @Test
    fun `unspotlight() returns if participantId does not exist`() = runTest {
        val roster = RosterImpl(
            scope = backgroundScope,
            event = event,
            participantId = participantId,
            store = store,
            step = object : InfinityService.ConferenceStep {},
        )
        roster.unspotlight(ParticipantId(Random.nextString(8)))
    }

    @Test
    fun `unspotlight() throws UnspotlightException`() = runTest {
        val participants = List(10) { Random.nextParticipant(it) }
        val causes = participants.associate { it.id to Throwable() }
        val roster = RosterImpl(
            scope = backgroundScope,
            event = event,
            participantId = participantId,
            store = store,
            step = object : InfinityService.ConferenceStep {

                override fun participant(participantId: ParticipantId): InfinityService.ParticipantStep {
                    assertThat(participantId, "participantId")
                        .isIn(*participants.map(Participant::id).toTypedArray())
                    return object : InfinityService.ParticipantStep {

                        override fun spotlightOff(token: Token): Call<Boolean> {
                            assertThat(token, "token").isEqualTo(store.get())
                            return object : TestCall<Boolean> {

                                override fun enqueue(callback: Callback<Boolean>) =
                                    callback.onFailure(this, causes.getValue(participantId))
                            }
                        }
                    }
                }
            },
        )
        roster.participants.test {
            event.subscriptionCount.first { it > 0 }
            assertThat(awaitItem(), "participants").isEmpty()
            participants.forEachIndexed { index, participant ->
                val response = participant.toParticipantResponse()
                val e = ParticipantCreateEvent(response)
                event.emit(e)
                assertThat(awaitItem(), "participants")
                    .index(index)
                    .isEqualTo(participant)
            }
        }
        participants.forEach {
            assertFailure { roster.unspotlight(it.id) }
                .isInstanceOf<UnspotlightException>()
                .hasCause(causes.getValue(it.id))
        }
    }

    @Test
    fun `unspotlight() returns`() = runTest {
        val participants = List(10) { Random.nextParticipant(it) }
        val roster = RosterImpl(
            scope = backgroundScope,
            event = event,
            participantId = participantId,
            store = store,
            step = object : InfinityService.ConferenceStep {

                override fun participant(participantId: ParticipantId): InfinityService.ParticipantStep {
                    assertThat(participantId, "participantId")
                        .isIn(*participants.map(Participant::id).toTypedArray())
                    return object : InfinityService.ParticipantStep {

                        override fun spotlightOff(token: Token): Call<Boolean> {
                            assertThat(token, "token").isEqualTo(store.get())
                            return object : TestCall<Boolean> {

                                override fun enqueue(callback: Callback<Boolean>) =
                                    callback.onSuccess(this, true)
                            }
                        }
                    }
                }
            },
        )
        roster.participants.test {
            event.subscriptionCount.first { it > 0 }
            assertThat(awaitItem(), "participants").isEmpty()
            participants.forEachIndexed { index, participant ->
                val response = participant.toParticipantResponse()
                val e = ParticipantCreateEvent(response)
                event.emit(e)
                assertThat(awaitItem(), "participants")
                    .index(index)
                    .isEqualTo(participant)
            }
        }
        participants.forEach { roster.unspotlight(it.id) }
    }

    @Test
    fun `lowerHand() returns if participantId does not exist`() = runTest {
        val roster = RosterImpl(
            scope = backgroundScope,
            event = event,
            participantId = participantId,
            store = store,
            step = object : InfinityService.ConferenceStep {},
        )
        roster.lowerHand(ParticipantId(Random.nextString(8)))
    }

    @Test
    fun `lowerHand() throws LowerHandException`() = runTest {
        val participants = List(10) { Random.nextParticipant(it) }
        val causes = participants.associate { it.id to Throwable() }
        val roster = RosterImpl(
            scope = backgroundScope,
            event = event,
            participantId = participantId,
            store = store,
            step = object : InfinityService.ConferenceStep {

                override fun participant(participantId: ParticipantId): InfinityService.ParticipantStep {
                    assertThat(participantId, "participantId")
                        .isIn(*participants.map(Participant::id).toTypedArray())
                    return object : InfinityService.ParticipantStep {

                        override fun clearBuzz(token: Token): Call<Boolean> {
                            assertThat(token, "token").isEqualTo(store.get())
                            return object : TestCall<Boolean> {

                                override fun enqueue(callback: Callback<Boolean>) =
                                    callback.onFailure(this, causes.getValue(participantId))
                            }
                        }
                    }
                }
            },
        )
        roster.participants.test {
            event.subscriptionCount.first { it > 0 }
            assertThat(awaitItem(), "participants").isEmpty()
            participants.forEachIndexed { index, participant ->
                val response = participant.toParticipantResponse()
                val e = ParticipantCreateEvent(response)
                event.emit(e)
                assertThat(awaitItem(), "participants")
                    .index(index)
                    .isEqualTo(participant)
            }
        }
        participants.forEach {
            assertFailure { roster.lowerHand(it.id) }
                .isInstanceOf<LowerHandException>()
                .hasCause(causes.getValue(it.id))
        }
    }

    @Test
    fun `lowerHand() returns`() = runTest {
        val participants = List(10) { Random.nextParticipant(it) }
        val roster = RosterImpl(
            scope = backgroundScope,
            event = event,
            participantId = participantId,
            store = store,
            step = object : InfinityService.ConferenceStep {

                override fun participant(participantId: ParticipantId): InfinityService.ParticipantStep {
                    assertThat(participantId, "participantId")
                        .isIn(*participants.map(Participant::id).toTypedArray())
                    return object : InfinityService.ParticipantStep {

                        override fun clearBuzz(token: Token): Call<Boolean> {
                            assertThat(token, "token").isEqualTo(store.get())
                            return object : TestCall<Boolean> {

                                override fun enqueue(callback: Callback<Boolean>) =
                                    callback.onSuccess(this, true)
                            }
                        }
                    }
                }
            },
        )
        roster.participants.test {
            event.subscriptionCount.first { it > 0 }
            assertThat(awaitItem(), "participants").isEmpty()
            participants.forEachIndexed { index, participant ->
                val response = participant.toParticipantResponse()
                val e = ParticipantCreateEvent(response)
                event.emit(e)
                assertThat(awaitItem(), "participants")
                    .index(index)
                    .isEqualTo(participant)
            }
        }
        participants.forEach { roster.lowerHand(it.id) }
    }

    @Test
    fun `lowerAllHands() throws LowerAllHandsException`() = runTest {
        val cause = Throwable()
        val roster = RosterImpl(
            scope = backgroundScope,
            event = event,
            participantId = participantId,
            store = store,
            step = object : InfinityService.ConferenceStep {

                override fun clearAllBuzz(token: Token): Call<Boolean> {
                    assertThat(token, "token").isEqualTo(store.get())
                    return object : TestCall<Boolean> {

                        override fun enqueue(callback: Callback<Boolean>) =
                            callback.onFailure(this, cause)
                    }
                }
            },
        )
        assertFailure { roster.lowerAllHands() }
            .isInstanceOf<LowerAllHandsException>()
            .hasCause(cause)
    }

    @Test
    fun `lowerAllHands() returns`() = runTest {
        val roster = RosterImpl(
            scope = backgroundScope,
            event = event,
            participantId = participantId,
            store = store,
            step = object : InfinityService.ConferenceStep {

                override fun clearAllBuzz(token: Token): Call<Boolean> {
                    assertThat(token, "token").isEqualTo(store.get())
                    return object : TestCall<Boolean> {

                        override fun enqueue(callback: Callback<Boolean>) =
                            callback.onSuccess(this, true)
                    }
                }
            },
        )
        roster.lowerAllHands()
    }

    @Test
    fun `lock() throws LockException`() = runTest {
        val cause = Throwable()
        val roster = RosterImpl(
            scope = backgroundScope,
            event = event,
            participantId = participantId,
            store = store,
            step = object : InfinityService.ConferenceStep {

                override fun lock(token: Token): Call<Boolean> {
                    assertThat(token, "token").isEqualTo(store.get())
                    return object : TestCall<Boolean> {

                        override fun enqueue(callback: Callback<Boolean>) =
                            callback.onFailure(this, cause)
                    }
                }
            },
        )
        assertFailure { roster.lock() }
            .isInstanceOf<LockException>()
            .hasCause(cause)
    }

    @Test
    fun `lock() returns`() = runTest {
        val roster = RosterImpl(
            scope = backgroundScope,
            event = event,
            participantId = participantId,
            store = store,
            step = object : InfinityService.ConferenceStep {

                override fun lock(token: Token): Call<Boolean> {
                    assertThat(token, "token").isEqualTo(store.get())
                    return object : TestCall<Boolean> {

                        override fun enqueue(callback: Callback<Boolean>) =
                            callback.onSuccess(this, true)
                    }
                }
            },
        )
        roster.lock()
    }

    @Test
    fun `unlock() throws UnlockException`() = runTest {
        val cause = Throwable()
        val roster = RosterImpl(
            scope = backgroundScope,
            event = event,
            participantId = participantId,
            store = store,
            step = object : InfinityService.ConferenceStep {

                override fun unlock(token: Token): Call<Boolean> {
                    assertThat(token, "token").isEqualTo(store.get())
                    return object : TestCall<Boolean> {

                        override fun enqueue(callback: Callback<Boolean>) =
                            callback.onFailure(this, cause)
                    }
                }
            },
        )
        assertFailure { roster.unlock() }
            .isInstanceOf<UnlockException>()
            .hasCause(cause)
    }

    @Test
    fun `unlock() returns`() = runTest {
        val roster = RosterImpl(
            scope = backgroundScope,
            event = event,
            participantId = participantId,
            store = store,
            step = object : InfinityService.ConferenceStep {

                override fun unlock(token: Token): Call<Boolean> {
                    assertThat(token, "token").isEqualTo(store.get())
                    return object : TestCall<Boolean> {

                        override fun enqueue(callback: Callback<Boolean>) =
                            callback.onSuccess(this, true)
                    }
                }
            },
        )
        roster.unlock()
    }

    @Test
    fun `muteAllGuests() throws MuteAllGuestsException`() = runTest {
        val cause = Throwable()
        val roster = RosterImpl(
            scope = backgroundScope,
            event = event,
            participantId = participantId,
            store = store,
            step = object : InfinityService.ConferenceStep {

                override fun muteGuests(token: Token): Call<Boolean> {
                    assertThat(token, "token").isEqualTo(store.get())
                    return object : TestCall<Boolean> {

                        override fun enqueue(callback: Callback<Boolean>) =
                            callback.onFailure(this, cause)
                    }
                }
            },
        )
        assertFailure { roster.muteAllGuests() }
            .isInstanceOf<MuteAllGuestsException>()
            .hasCause(cause)
    }

    @Test
    fun `muteAllGuests() returns`() = runTest {
        val roster = RosterImpl(
            scope = backgroundScope,
            event = event,
            participantId = participantId,
            store = store,
            step = object : InfinityService.ConferenceStep {

                override fun muteGuests(token: Token): Call<Boolean> {
                    assertThat(token, "token").isEqualTo(store.get())
                    return object : TestCall<Boolean> {

                        override fun enqueue(callback: Callback<Boolean>) =
                            callback.onSuccess(this, true)
                    }
                }
            },
        )
        roster.muteAllGuests()
    }

    @Test
    fun `unmuteAllGuests() throws UnmuteAllGuestsException`() = runTest {
        val cause = Throwable()
        val roster = RosterImpl(
            scope = backgroundScope,
            event = event,
            participantId = participantId,
            store = store,
            step = object : InfinityService.ConferenceStep {

                override fun unmuteGuests(token: Token): Call<Boolean> {
                    assertThat(token, "token").isEqualTo(store.get())
                    return object : TestCall<Boolean> {

                        override fun enqueue(callback: Callback<Boolean>) =
                            callback.onFailure(this, cause)
                    }
                }
            },
        )
        assertFailure { roster.unmuteAllGuests() }
            .isInstanceOf<UnmuteAllGuestsException>()
            .hasCause(cause)
    }

    @Test
    fun `unmuteAllGuests() returns`() = runTest {
        val roster = RosterImpl(
            scope = backgroundScope,
            event = event,
            participantId = participantId,
            store = store,
            step = object : InfinityService.ConferenceStep {

                override fun unmuteGuests(token: Token): Call<Boolean> {
                    assertThat(token, "token").isEqualTo(store.get())
                    return object : TestCall<Boolean> {

                        override fun enqueue(callback: Callback<Boolean>) =
                            callback.onSuccess(this, true)
                    }
                }
            },
        )
        roster.unmuteAllGuests()
    }

    @Test
    fun `disconnectAll() throws DisconnectAllException`() = runTest {
        val cause = Throwable()
        val roster = RosterImpl(
            scope = backgroundScope,
            event = event,
            participantId = participantId,
            store = store,
            step = object : InfinityService.ConferenceStep {

                override fun disconnect(token: Token): Call<Boolean> {
                    assertThat(token, "token").isEqualTo(store.get())
                    return object : TestCall<Boolean> {

                        override fun enqueue(callback: Callback<Boolean>) =
                            callback.onFailure(this, cause)
                    }
                }
            },
        )
        assertFailure { roster.disconnectAll() }
            .isInstanceOf<DisconnectAllException>()
            .hasCause(cause)
    }

    @Test
    fun `disconnectAll() returns`() = runTest {
        val roster = RosterImpl(
            scope = backgroundScope,
            event = event,
            participantId = participantId,
            store = store,
            step = object : InfinityService.ConferenceStep {

                override fun disconnect(token: Token): Call<Boolean> {
                    assertThat(token, "token").isEqualTo(store.get())
                    return object : TestCall<Boolean> {

                        override fun enqueue(callback: Callback<Boolean>) =
                            callback.onSuccess(this, true)
                    }
                }
            },
        )
        roster.disconnectAll()
    }

    private fun Random.nextParticipant(
        index: Int = 0,
        id: ParticipantId = if (index == 0) participantId else ParticipantId(Random.nextString(8)),
    ): Participant {
        val startTime = Instant.fromEpochSeconds(index.toLong())
        return Participant(
            id = id,
            startTime = startTime,
            role = Role.entries.random(this),
            serviceType = ServiceType.entries.random(this),
            buzzTime = startTime + nextInt(0, 100).seconds,
            spotlightTime = startTime + nextInt(0, 100).seconds,
            displayName = nextString(8),
            overlayText = nextString(8),
            me = id == participantId,
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
