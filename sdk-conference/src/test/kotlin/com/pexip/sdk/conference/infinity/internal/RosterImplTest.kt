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
import assertk.Table2
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
import assertk.tableOf
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
import com.pexip.sdk.conference.MuteVideoException
import com.pexip.sdk.conference.Participant
import com.pexip.sdk.conference.RaiseHandException
import com.pexip.sdk.conference.SpotlightException
import com.pexip.sdk.conference.UnlockException
import com.pexip.sdk.conference.UnmuteAllGuestsException
import com.pexip.sdk.conference.UnmuteException
import com.pexip.sdk.conference.UnmuteVideoException
import com.pexip.sdk.conference.UnspotlightException
import com.pexip.sdk.infinity.ParticipantId
import com.pexip.sdk.infinity.Role
import com.pexip.sdk.infinity.ServiceType
import com.pexip.sdk.infinity.test.nextParticipantId
import com.pexip.sdk.infinity.test.nextString
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import kotlin.random.Random
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds

class RosterImplTest {

    private lateinit var event: MutableSharedFlow<Event>
    private lateinit var store: TokenStore
    private lateinit var table: Table2<ParticipantId, ParticipantId?>

    @BeforeTest
    fun setUp() {
        event = MutableSharedFlow(extraBufferCapacity = 1)
        store = TokenStore(Random.nextToken())
        table = tableOf("participantId", "parentParticipantId")
            .row<ParticipantId, ParticipantId?>(Random.nextParticipantId(), null)
            .row(Random.nextParticipantId(), Random.nextParticipantId())
    }

    @Test
    fun `does not update the list until syncing is finished`() {
        table.forAll(::`does not update the list until syncing is finished`)
    }

    @Test
    fun `create, update, delete, stage correctly modify the list`() {
        table.forAll(::`create, update, delete, stage correctly modify the list`)
    }

    @Test
    fun `update without a matching create does not modify the list`() {
        table.forAll(::`update without a matching create does not modify the list`)
    }

    @Test
    fun `me produces the correct participant`() {
        table.forAll(::`me produces the correct participant`)
    }

    @Test
    fun `locked produces the correct lock state`() {
        table.forAll(::`locked produces the correct lock state`)
    }

    @Test
    fun `allGuestsMuted produces the correct all guests muted state`() {
        table.forAll(::`allGuestsMuted produces the correct all guests muted state`)
    }

    @Test
    fun `raiseHand() returns if participantId does not exist`() {
        table.forAll(::`raiseHand() returns if participantId does not exist`)
    }

    @Test
    fun `raiseHand() throws RaiseHandException`() {
        table.forAll(::`raiseHand() throws RaiseHandException`)
    }

    @Test
    fun `raiseHand() returns`() {
        table.forAll(::`raiseHand() returns`)
    }

    @Test
    fun `admit() returns if participantId does not exist`() {
        table.forAll(::`admit() returns if participantId does not exist`)
    }

    @Test
    fun `admit() throws AdmitException`() {
        table.forAll(::`admit() throws AdmitException`)
    }

    @Test
    fun `admit() returns`() {
        table.forAll(::`admit() returns`)
    }

    @Test
    fun `disconnect() returns if participantId does not exist`() {
        table.forAll(::`disconnect() returns if participantId does not exist`)
    }

    @Test
    fun `disconnect() throws DisconnectException`() {
        table.forAll(::`disconnect() throws DisconnectException`)
    }

    @Test
    fun `disconnect() returns`() {
        table.forAll(::`disconnect() returns`)
    }

    @Test
    fun `makeHost() throws MakeHostException`() {
        table.forAll(::`makeHost() throws MakeHostException`)
    }

    @Test
    fun `makeHost() returns`() {
        table.forAll(::`makeGuest() returns`)
    }

    @Test
    fun `makeGuest() throws MakeGuestException`() {
        table.forAll(::`makeHost() throws MakeHostException`)
    }

    @Test
    fun `makeGuest() returns`() {
        table.forAll(::`makeGuest() returns`)
    }

    @Test
    fun `mute() returns if participantId does not exist`() {
        table.forAll(::`mute() returns if participantId does not exist`)
    }

    @Test
    fun `mute() throws MuteException`() {
        table.forAll(::`mute() throws MuteException`)
    }

    @Test
    fun `mute() returns`() {
        table.forAll(::`mute() returns`)
    }

    @Test
    fun `unmute() returns if participantId does not exist`() {
        table.forAll(::`unmute() returns if participantId does not exist`)
    }

    @Test
    fun `unmute() throws UnmuteException`() {
        table.forAll(::`unmute() throws UnmuteException`)
    }

    @Test
    fun `unmute() returns`() {
        table.forAll(::`unmute() returns`)
    }

    @Test
    fun `muteVideo() returns if participantId does not exist`() {
        table.forAll(::`muteVideo() returns if participantId does not exist`)
    }

    @Test
    fun `muteVideo() throws MuteVideoException`() {
        table.forAll(::`muteVideo() throws MuteVideoException`)
    }

    @Test
    fun `muteVideo() returns`() {
        table.forAll(::`muteVideo() returns`)
    }

    @Test
    fun `unmuteVideo() returns if participantId does not exist`() {
        table.forAll(::`unmuteVideo() returns if participantId does not exist`)
    }

    @Test
    fun `unmuteVideo() throws UnmuteVideoException`() {
        table.forAll(::`unmuteVideo() throws UnmuteVideoException`)
    }

    @Test
    fun `unmuteVideo() returns`() {
        table.forAll(::`unmuteVideo() returns`)
    }

    @Test
    fun `spotlight() returns if participantId does not exist`() {
        table.forAll(::`spotlight() returns if participantId does not exist`)
    }

    @Test
    fun `spotlight() throws SpotlightException`() {
        table.forAll(::`spotlight() throws SpotlightException`)
    }

    @Test
    fun `spotlight() returns`() {
        table.forAll(::`spotlight() returns`)
    }

    @Test
    fun `unspotlight() returns if participantId does not exist`() {
        table.forAll(::`unspotlight() returns if participantId does not exist`)
    }

    @Test
    fun `unspotlight() throws UnspotlightException`() {
        table.forAll(::`unspotlight() throws UnspotlightException`)
    }

    @Test
    fun `unspotlight() returns`() {
        table.forAll(::`unspotlight() returns`)
    }

    @Test
    fun `lowerHand() returns if participantId does not exist`() {
        table.forAll(::`lowerHand() returns if participantId does not exist`)
    }

    @Test
    fun `lowerHand() throws LowerHandException`() {
        table.forAll(::`lowerHand() throws LowerHandException`)
    }

    @Test
    fun `lowerHand() returns`() {
        table.forAll(::`lowerHand() returns`)
    }

    @Test
    fun `lowerAllHands() throws LowerAllHandsException`() {
        table.forAll(::`lowerAllHands() throws LowerAllHandsException`)
    }

    @Test
    fun `lowerAllHands() returns`() {
        table.forAll(::`lowerAllHands() returns`)
    }

    @Test
    fun `lock() throws LockException`() {
        table.forAll(::`lock() throws LockException`)
    }

    @Test
    fun `lock() returns`() {
        table.forAll(::`lock() returns`)
    }

    @Test
    fun `unlock() throws UnlockException`() {
        table.forAll(::`unlock() throws UnlockException`)
    }

    @Test
    fun `unlock() returns`() {
        table.forAll(::`unlock() returns`)
    }

    @Test
    fun `muteAllGuests() throws MuteAllGuestsException`() {
        table.forAll(::`muteAllGuests() throws MuteAllGuestsException`)
    }

    @Test
    fun `muteAllGuests() returns`() {
        table.forAll(::`muteAllGuests() returns`)
    }

    @Test
    fun `unmuteAllGuests() throws UnmuteAllGuestsException`() {
        table.forAll(::`unmuteAllGuests() throws UnmuteAllGuestsException`)
    }

    @Test
    fun `unmuteAllGuests() returns`() {
        table.forAll(::`unmuteAllGuests() returns`)
    }

    @Test
    fun `disconnectAll() throws DisconnectAllException`() {
        table.forAll(::`disconnectAll() throws DisconnectAllException`)
    }

    @Test
    fun `disconnectAll() returns`() {
        table.forAll(::`disconnectAll() returns`)
    }

    private fun `does not update the list until syncing is finished`(
        participantId: ParticipantId,
        parentParticipantId: ParticipantId?,
    ) = runTest {
        val roster = RosterImpl(participantId, parentParticipantId)
        roster.participants.test {
            event.subscriptionCount.first { it > 0 }
            assertThat(awaitItem(), "participants").isEmpty()
            event.emit(ParticipantSyncBeginEvent)
            val participants = Random.nextParticipantList(participantId, parentParticipantId)
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

    private fun `create, update, delete, stage correctly modify the list`(
        participantId: ParticipantId,
        parentParticipantId: ParticipantId?,
    ) = runTest {
        val roster = RosterImpl(participantId, parentParticipantId)
        roster.participants.test {
            event.subscriptionCount.first { it > 0 }
            assertThat(awaitItem(), "participants").isEmpty()
            var participant = Random.nextParticipant(
                id = parentParticipantId ?: participantId,
                me = true,
            )
            var response = participant.toParticipantResponse()
            var e: Event = ParticipantCreateEvent(response)
            event.emit(e)
            assertThat(awaitItem(), "participants").containsOnly(participant)
            participant = Random.nextParticipant(id = participant.id, me = participant.me)
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

    private fun `update without a matching create does not modify the list`(
        participantId: ParticipantId,
        parentParticipantId: ParticipantId?,
    ) = runTest {
        val roster = RosterImpl(participantId, parentParticipantId)
        roster.participants.test {
            event.subscriptionCount.first { it > 0 }
            assertThat(awaitItem(), "participants").isEmpty()
            val participant = Random.nextParticipant(id = parentParticipantId ?: participantId)
            val response = participant.toParticipantResponse()
            val e = ParticipantUpdateEvent(response)
            event.emit(e)
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun `me produces the correct participant`(
        participantId: ParticipantId,
        parentParticipantId: ParticipantId?,
    ) = runTest {
        val roster = RosterImpl(participantId, parentParticipantId)
        roster.me.test {
            event.subscriptionCount.first { it > 0 }
            assertThat(awaitItem(), "me").isNull()
            var me = Random.nextParticipant(id = parentParticipantId ?: participantId, me = true)
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

    private fun `locked produces the correct lock state`(
        participantId: ParticipantId,
        parentParticipantId: ParticipantId?,
    ) = runTest {
        val roster = RosterImpl(participantId, parentParticipantId)
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

    private fun `allGuestsMuted produces the correct all guests muted state`(
        participantId: ParticipantId,
        parentParticipantId: ParticipantId?,
    ) = runTest {
        val roster = RosterImpl(participantId, parentParticipantId)
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

    private fun `raiseHand() returns if participantId does not exist`(
        participantId: ParticipantId,
        parentParticipantId: ParticipantId?,
    ) = runTest {
        val roster = RosterImpl(participantId, parentParticipantId)
        roster.raiseHand(Random.nextParticipantId())
    }

    private fun `raiseHand() throws RaiseHandException`(
        participantId: ParticipantId,
        parentParticipantId: ParticipantId?,
    ) = runTest {
        val participants = Random.nextParticipantList(participantId, parentParticipantId)
        val participantIds = participants.toParticipantIdSet(participantId)
        val causes = participantIds.associateWith { Throwable() }
        val roster = RosterImpl(
            participantId = participantId,
            parentParticipantId = parentParticipantId,
            step = object : InfinityService.ConferenceStep {

                override fun participant(participantId: ParticipantId): InfinityService.ParticipantStep {
                    assertThat(participantId, "participantId").isIn(*participantIds.toTypedArray())
                    return object : InfinityService.ParticipantStep {

                        override fun buzz(token: Token): Call<Boolean> {
                            assertThat(token, "token").isEqualTo(store.token.value)
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

    private fun `raiseHand() returns`(
        participantId: ParticipantId,
        parentParticipantId: ParticipantId?,
    ) = runTest {
        val participants = Random.nextParticipantList(participantId, parentParticipantId)
        val participantIds = participants.toParticipantIdSet(participantId)
        val roster = RosterImpl(
            participantId = participantId,
            parentParticipantId = parentParticipantId,
            step = object : InfinityService.ConferenceStep {

                override fun participant(participantId: ParticipantId): InfinityService.ParticipantStep {
                    assertThat(participantId, "participantId").isIn(*participantIds.toTypedArray())
                    return object : InfinityService.ParticipantStep {

                        override fun buzz(token: Token): Call<Boolean> {
                            assertThat(token, "token").isEqualTo(store.token.value)
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

    private fun `admit() returns if participantId does not exist`(
        participantId: ParticipantId,
        parentParticipantId: ParticipantId?,
    ) = runTest {
        val roster = RosterImpl(participantId, parentParticipantId)
        roster.disconnect(Random.nextParticipantId())
    }

    private fun `admit() throws AdmitException`(
        participantId: ParticipantId,
        parentParticipantId: ParticipantId?,
    ) = runTest {
        val participants = Random.nextParticipantList(participantId, parentParticipantId)
        val participantIds = participants.toParticipantIdSet(participantId)
        val causes = participantIds.associateWith { Throwable() }
        val roster = RosterImpl(
            participantId = participantId,
            parentParticipantId = parentParticipantId,
            step = object : InfinityService.ConferenceStep {

                override fun participant(participantId: ParticipantId): InfinityService.ParticipantStep {
                    assertThat(participantId, "participantId").isIn(*participantIds.toTypedArray())
                    return object : InfinityService.ParticipantStep {

                        override fun unlock(token: Token): Call<Boolean> {
                            assertThat(token, "token").isEqualTo(store.token.value)
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

    private fun `admit() returns`(
        participantId: ParticipantId,
        parentParticipantId: ParticipantId?,
    ) = runTest {
        val participants = Random.nextParticipantList(participantId, parentParticipantId)
        val participantIds = participants.toParticipantIdSet(participantId)
        val roster = RosterImpl(
            participantId = participantId,
            parentParticipantId = parentParticipantId,
            step = object : InfinityService.ConferenceStep {

                override fun participant(participantId: ParticipantId): InfinityService.ParticipantStep {
                    assertThat(participantId, "participantId").isIn(*participantIds.toTypedArray())
                    return object : InfinityService.ParticipantStep {

                        override fun unlock(token: Token): Call<Boolean> {
                            assertThat(token, "token").isEqualTo(store.token.value)
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

    private fun `disconnect() returns if participantId does not exist`(
        participantId: ParticipantId,
        parentParticipantId: ParticipantId?,
    ) = runTest {
        val roster = RosterImpl(participantId, parentParticipantId)
        roster.disconnect(Random.nextParticipantId())
    }

    private fun `disconnect() throws DisconnectException`(
        participantId: ParticipantId,
        parentParticipantId: ParticipantId?,
    ) = runTest {
        val participants = Random.nextParticipantList(participantId, parentParticipantId)
        val participantIds = participants.toParticipantIdSet(participantId)
        val causes = participantIds.associateWith { Throwable() }
        val roster = RosterImpl(
            participantId = participantId,
            parentParticipantId = parentParticipantId,
            step = object : InfinityService.ConferenceStep {

                override fun participant(participantId: ParticipantId): InfinityService.ParticipantStep {
                    assertThat(participantId, "participantId").isIn(*participantIds.toTypedArray())
                    return object : InfinityService.ParticipantStep {

                        override fun disconnect(token: Token): Call<Boolean> {
                            assertThat(token, "token").isEqualTo(store.token.value)
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

    private fun `disconnect() returns`(
        participantId: ParticipantId,
        parentParticipantId: ParticipantId?,
    ) = runTest {
        val participants = Random.nextParticipantList(participantId, parentParticipantId)
        val participantIds = participants.toParticipantIdSet(participantId)
        val roster = RosterImpl(
            participantId = participantId,
            parentParticipantId = parentParticipantId,
            step = object : InfinityService.ConferenceStep {

                override fun participant(participantId: ParticipantId): InfinityService.ParticipantStep {
                    assertThat(participantId, "participantId").isIn(*participantIds.toTypedArray())
                    return object : InfinityService.ParticipantStep {

                        override fun disconnect(token: Token): Call<Boolean> {
                            assertThat(token, "token").isEqualTo(store.token.value)
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

    private fun `makeHost() throws MakeHostException`(
        participantId: ParticipantId,
        parentParticipantId: ParticipantId?,
    ) = runTest {
        val participants = Random.nextParticipantList(participantId, parentParticipantId)
        val participantIds = participants.toParticipantIdSet(participantId)
        val causes = participantIds.associateWith { Throwable() }
        val roster = RosterImpl(
            participantId = participantId,
            parentParticipantId = parentParticipantId,
            step = object : InfinityService.ConferenceStep {

                override fun participant(participantId: ParticipantId): InfinityService.ParticipantStep {
                    assertThat(participantId, "participantId").isIn(*participantIds.toTypedArray())
                    return object : InfinityService.ParticipantStep {

                        override fun role(request: RoleRequest, token: Token): Call<Boolean> {
                            assertThat(token, "token").isEqualTo(store.token.value)
                            assertThat(request::role, "request").isEqualTo(Role.HOST)
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

    private fun `makeHost() returns`(
        participantId: ParticipantId,
        parentParticipantId: ParticipantId?,
    ) = runTest {
        val participants = Random.nextParticipantList(participantId, parentParticipantId)
        val participantIds = participants.toParticipantIdSet(participantId)
        val roster = RosterImpl(
            participantId = participantId,
            parentParticipantId = parentParticipantId,
            step = object : InfinityService.ConferenceStep {

                override fun participant(participantId: ParticipantId): InfinityService.ParticipantStep {
                    assertThat(participantId, "participantId").isIn(*participantIds.toTypedArray())
                    return object : InfinityService.ParticipantStep {

                        override fun role(request: RoleRequest, token: Token): Call<Boolean> {
                            assertThat(token, "token").isEqualTo(store.token.value)
                            assertThat(request::role).isEqualTo(Role.HOST)
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

    private fun `makeGuest() throws MakeGuestException`(
        participantId: ParticipantId,
        parentParticipantId: ParticipantId?,
    ) = runTest {
        val participants = Random.nextParticipantList(participantId, parentParticipantId)
        val participantIds = participants.toParticipantIdSet(participantId)
        val causes = participantIds.associateWith { Throwable() }
        val roster = RosterImpl(
            participantId = participantId,
            parentParticipantId = parentParticipantId,
            step = object : InfinityService.ConferenceStep {

                override fun participant(participantId: ParticipantId): InfinityService.ParticipantStep {
                    assertThat(participantId, "participantId").isIn(*participantIds.toTypedArray())
                    return object : InfinityService.ParticipantStep {

                        override fun role(request: RoleRequest, token: Token): Call<Boolean> {
                            assertThat(token, "token").isEqualTo(store.token.value)
                            assertThat(request::role, "request").isEqualTo(Role.GUEST)
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

    private fun `makeGuest() returns`(
        participantId: ParticipantId,
        parentParticipantId: ParticipantId?,
    ) = runTest {
        val participants = Random.nextParticipantList(participantId, parentParticipantId)
        val participantIds = participants.toParticipantIdSet(participantId)
        val roster = RosterImpl(
            participantId = participantId,
            parentParticipantId = parentParticipantId,
            step = object : InfinityService.ConferenceStep {

                override fun participant(participantId: ParticipantId): InfinityService.ParticipantStep {
                    assertThat(participantId, "participantId").isIn(*participantIds.toTypedArray())
                    return object : InfinityService.ParticipantStep {

                        override fun role(request: RoleRequest, token: Token): Call<Boolean> {
                            assertThat(token, "token").isEqualTo(store.token.value)
                            assertThat(request::role).isEqualTo(Role.GUEST)
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

    private fun `mute() returns if participantId does not exist`(
        participantId: ParticipantId,
        parentParticipantId: ParticipantId?,
    ) = runTest {
        val roster = RosterImpl(participantId, parentParticipantId)
        roster.mute(Random.nextParticipantId())
    }

    private fun `mute() throws MuteException`(
        participantId: ParticipantId,
        parentParticipantId: ParticipantId?,
    ) = runTest {
        val participants = Random.nextParticipantList(participantId, parentParticipantId)
        val participantIds = participants.toParticipantIdSet(participantId)
        val causes = participantIds.associateWith { Throwable() }
        val roster = RosterImpl(
            participantId = participantId,
            parentParticipantId = parentParticipantId,
            step = object : InfinityService.ConferenceStep {

                override fun participant(participantId: ParticipantId): InfinityService.ParticipantStep {
                    assertThat(participantId, "participantId").isIn(*participantIds.toTypedArray())
                    return object : InfinityService.ParticipantStep {

                        override fun mute(token: Token): Call<Unit> {
                            assertThat(token, "token").isEqualTo(store.token.value)
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

    private fun `mute() returns`(
        participantId: ParticipantId,
        parentParticipantId: ParticipantId?,
    ) = runTest {
        val participants = Random.nextParticipantList(participantId, parentParticipantId)
        val participantIds = participants.toParticipantIdSet(participantId)
        val roster = RosterImpl(
            participantId = participantId,
            parentParticipantId = parentParticipantId,
            step = object : InfinityService.ConferenceStep {

                override fun participant(participantId: ParticipantId): InfinityService.ParticipantStep {
                    assertThat(participantId, "participantId").isIn(*participantIds.toTypedArray())
                    return object : InfinityService.ParticipantStep {

                        override fun mute(token: Token): Call<Unit> {
                            assertThat(token, "token").isEqualTo(store.token.value)
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

    private fun `unmute() returns if participantId does not exist`(
        participantId: ParticipantId,
        parentParticipantId: ParticipantId?,
    ) = runTest {
        val roster = RosterImpl(participantId, parentParticipantId)
        roster.unmute(Random.nextParticipantId())
    }

    private fun `unmute() throws UnmuteException`(
        participantId: ParticipantId,
        parentParticipantId: ParticipantId?,
    ) = runTest {
        val participants = Random.nextParticipantList(participantId, parentParticipantId)
        val participantIds = participants.toParticipantIdSet(participantId)
        val causes = participantIds.associateWith { Throwable() }
        val roster = RosterImpl(
            participantId = participantId,
            parentParticipantId = parentParticipantId,
            step = object : InfinityService.ConferenceStep {

                override fun participant(participantId: ParticipantId): InfinityService.ParticipantStep {
                    assertThat(participantId, "participantId").isIn(*participantIds.toTypedArray())
                    return object : InfinityService.ParticipantStep {

                        override fun unmute(token: Token): Call<Unit> {
                            assertThat(token, "token").isEqualTo(store.token.value)
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

    private fun `unmute() returns`(
        participantId: ParticipantId,
        parentParticipantId: ParticipantId?,
    ) = runTest {
        val participants = Random.nextParticipantList(participantId, parentParticipantId)
        val participantIds = participants.toParticipantIdSet(participantId)
        val roster = RosterImpl(
            participantId = participantId,
            parentParticipantId = parentParticipantId,
            step = object : InfinityService.ConferenceStep {

                override fun participant(participantId: ParticipantId): InfinityService.ParticipantStep {
                    assertThat(participantId, "participantId").isIn(*participantIds.toTypedArray())
                    return object : InfinityService.ParticipantStep {

                        override fun unmute(token: Token): Call<Unit> {
                            assertThat(token, "token").isEqualTo(store.token.value)
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

    private fun `muteVideo() returns if participantId does not exist`(
        participantId: ParticipantId,
        parentParticipantId: ParticipantId?,
    ) = runTest {
        val roster = RosterImpl(participantId, parentParticipantId)
        roster.muteVideo(Random.nextParticipantId())
    }

    private fun `muteVideo() throws MuteVideoException`(
        participantId: ParticipantId,
        parentParticipantId: ParticipantId?,
    ) = runTest {
        val participants = Random.nextParticipantList(participantId, parentParticipantId)
        val participantIds = participants.toParticipantIdSet(participantId)
        val causes = participantIds.associateWith { Throwable() }
        val roster = RosterImpl(
            participantId = participantId,
            parentParticipantId = parentParticipantId,
            step = object : InfinityService.ConferenceStep {

                override fun participant(participantId: ParticipantId): InfinityService.ParticipantStep {
                    assertThat(participantId, "participantId").isIn(*participantIds.toTypedArray())
                    return object : InfinityService.ParticipantStep {

                        override fun videoMuted(token: Token): Call<Unit> {
                            assertThat(token, "token").isEqualTo(store.token.value)
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
            assertFailure { roster.muteVideo(it.id) }
                .isInstanceOf<MuteVideoException>()
                .hasCause(causes.getValue(it.id))
        }
    }

    private fun `muteVideo() returns`(
        participantId: ParticipantId,
        parentParticipantId: ParticipantId?,
    ) = runTest {
        val participants = Random.nextParticipantList(participantId, parentParticipantId)
        val participantIds = participants.toParticipantIdSet(participantId)
        val roster = RosterImpl(
            participantId = participantId,
            parentParticipantId = parentParticipantId,
            step = object : InfinityService.ConferenceStep {

                override fun participant(participantId: ParticipantId): InfinityService.ParticipantStep {
                    assertThat(participantId, "participantId").isIn(*participantIds.toTypedArray())
                    return object : InfinityService.ParticipantStep {

                        override fun videoMuted(token: Token): Call<Unit> {
                            assertThat(token, "token").isEqualTo(store.token.value)
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
        participants.forEach { roster.muteVideo(it.id) }
    }

    private fun `unmuteVideo() returns if participantId does not exist`(
        participantId: ParticipantId,
        parentParticipantId: ParticipantId?,
    ) = runTest {
        val roster = RosterImpl(participantId, parentParticipantId)
        roster.unmuteVideo(Random.nextParticipantId())
    }

    private fun `unmuteVideo() throws UnmuteVideoException`(
        participantId: ParticipantId,
        parentParticipantId: ParticipantId?,
    ) = runTest {
        val participants = Random.nextParticipantList(participantId, parentParticipantId)
        val participantIds = participants.toParticipantIdSet(participantId)
        val causes = participantIds.associateWith { Throwable() }
        val roster = RosterImpl(
            participantId = participantId,
            parentParticipantId = parentParticipantId,
            step = object : InfinityService.ConferenceStep {

                override fun participant(participantId: ParticipantId): InfinityService.ParticipantStep {
                    assertThat(participantId, "participantId").isIn(*participantIds.toTypedArray())
                    return object : InfinityService.ParticipantStep {

                        override fun videoUnmuted(token: Token): Call<Unit> {
                            assertThat(token, "token").isEqualTo(store.token.value)
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
            assertFailure { roster.unmuteVideo(it.id) }
                .isInstanceOf<UnmuteVideoException>()
                .hasCause(causes.getValue(it.id))
        }
    }

    private fun `unmuteVideo() returns`(
        participantId: ParticipantId,
        parentParticipantId: ParticipantId?,
    ) = runTest {
        val participants = Random.nextParticipantList(participantId, parentParticipantId)
        val participantIds = participants.toParticipantIdSet(participantId)
        val roster = RosterImpl(
            participantId = participantId,
            parentParticipantId = parentParticipantId,
            step = object : InfinityService.ConferenceStep {

                override fun participant(participantId: ParticipantId): InfinityService.ParticipantStep {
                    assertThat(participantId, "participantId").isIn(*participantIds.toTypedArray())
                    return object : InfinityService.ParticipantStep {

                        override fun videoUnmuted(token: Token): Call<Unit> {
                            assertThat(token, "token").isEqualTo(store.token.value)
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
        participants.forEach { roster.unmuteVideo(it.id) }
    }

    private fun `spotlight() returns if participantId does not exist`(
        participantId: ParticipantId,
        parentParticipantId: ParticipantId?,
    ) = runTest {
        val roster = RosterImpl(participantId, parentParticipantId)
        roster.spotlight(Random.nextParticipantId())
    }

    private fun `spotlight() throws SpotlightException`(
        participantId: ParticipantId,
        parentParticipantId: ParticipantId?,
    ) = runTest {
        val participants = Random.nextParticipantList(participantId, parentParticipantId)
        val participantIds = participants.toParticipantIdSet(participantId)
        val causes = participantIds.associateWith { Throwable() }
        val roster = RosterImpl(
            participantId = participantId,
            parentParticipantId = parentParticipantId,
            step = object : InfinityService.ConferenceStep {

                override fun participant(participantId: ParticipantId): InfinityService.ParticipantStep {
                    assertThat(participantId, "participantId").isIn(*participantIds.toTypedArray())
                    return object : InfinityService.ParticipantStep {

                        override fun spotlightOn(token: Token): Call<Boolean> {
                            assertThat(token, "token").isEqualTo(store.token.value)
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

    private fun `spotlight() returns`(
        participantId: ParticipantId,
        parentParticipantId: ParticipantId?,
    ) = runTest {
        val participants = Random.nextParticipantList(participantId, parentParticipantId)
        val participantIds = participants.toParticipantIdSet(participantId)
        val roster = RosterImpl(
            participantId = participantId,
            parentParticipantId = parentParticipantId,
            step = object : InfinityService.ConferenceStep {

                override fun participant(participantId: ParticipantId): InfinityService.ParticipantStep {
                    assertThat(participantId, "participantId").isIn(*participantIds.toTypedArray())
                    return object : InfinityService.ParticipantStep {

                        override fun spotlightOn(token: Token): Call<Boolean> {
                            assertThat(token, "token").isEqualTo(store.token.value)
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

    private fun `unspotlight() returns if participantId does not exist`(
        participantId: ParticipantId,
        parentParticipantId: ParticipantId?,
    ) = runTest {
        val roster = RosterImpl(participantId, parentParticipantId)
        roster.unspotlight(Random.nextParticipantId())
    }

    private fun `unspotlight() throws UnspotlightException`(
        participantId: ParticipantId,
        parentParticipantId: ParticipantId?,
    ) = runTest {
        val participants = Random.nextParticipantList(participantId, parentParticipantId)
        val participantIds = participants.toParticipantIdSet(participantId)
        val causes = participantIds.associateWith { Throwable() }
        val roster = RosterImpl(
            participantId = participantId,
            parentParticipantId = parentParticipantId,
            step = object : InfinityService.ConferenceStep {

                override fun participant(participantId: ParticipantId): InfinityService.ParticipantStep {
                    assertThat(participantId, "participantId").isIn(*participantIds.toTypedArray())
                    return object : InfinityService.ParticipantStep {

                        override fun spotlightOff(token: Token): Call<Boolean> {
                            assertThat(token, "token").isEqualTo(store.token.value)
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

    private fun `unspotlight() returns`(
        participantId: ParticipantId,
        parentParticipantId: ParticipantId?,
    ) = runTest {
        val participants = Random.nextParticipantList(participantId, parentParticipantId)
        val participantIds = participants.toParticipantIdSet(participantId)
        val roster = RosterImpl(
            participantId = participantId,
            parentParticipantId = parentParticipantId,
            step = object : InfinityService.ConferenceStep {

                override fun participant(participantId: ParticipantId): InfinityService.ParticipantStep {
                    assertThat(participantId, "participantId").isIn(*participantIds.toTypedArray())
                    return object : InfinityService.ParticipantStep {

                        override fun spotlightOff(token: Token): Call<Boolean> {
                            assertThat(token, "token").isEqualTo(store.token.value)
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

    private fun `lowerHand() returns if participantId does not exist`(
        participantId: ParticipantId,
        parentParticipantId: ParticipantId?,
    ) = runTest {
        val roster = RosterImpl(participantId, parentParticipantId)
        roster.lowerHand(Random.nextParticipantId())
    }

    private fun `lowerHand() throws LowerHandException`(
        participantId: ParticipantId,
        parentParticipantId: ParticipantId?,
    ) = runTest {
        val participants = Random.nextParticipantList(participantId, parentParticipantId)
        val participantIds = participants.toParticipantIdSet(participantId)
        val causes = participantIds.associateWith { Throwable() }
        val roster = RosterImpl(
            participantId = participantId,
            parentParticipantId = parentParticipantId,
            step = object : InfinityService.ConferenceStep {

                override fun participant(participantId: ParticipantId): InfinityService.ParticipantStep {
                    assertThat(participantId, "participantId").isIn(*participantIds.toTypedArray())
                    return object : InfinityService.ParticipantStep {

                        override fun clearBuzz(token: Token): Call<Boolean> {
                            assertThat(token, "token").isEqualTo(store.token.value)
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

    private fun `lowerHand() returns`(
        participantId: ParticipantId,
        parentParticipantId: ParticipantId?,
    ) = runTest {
        val participants = Random.nextParticipantList(participantId, parentParticipantId)
        val participantIds = participants.toParticipantIdSet(participantId)
        val roster = RosterImpl(
            participantId = participantId,
            parentParticipantId = parentParticipantId,
            step = object : InfinityService.ConferenceStep {

                override fun participant(participantId: ParticipantId): InfinityService.ParticipantStep {
                    assertThat(participantId, "participantId").isIn(*participantIds.toTypedArray())
                    return object : InfinityService.ParticipantStep {

                        override fun clearBuzz(token: Token): Call<Boolean> {
                            assertThat(token, "token").isEqualTo(store.token.value)
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

    private fun `lowerAllHands() throws LowerAllHandsException`(
        participantId: ParticipantId,
        parentParticipantId: ParticipantId?,
    ) = runTest {
        val cause = Throwable()
        val roster = RosterImpl(
            participantId = participantId,
            parentParticipantId = parentParticipantId,
            step = object : InfinityService.ConferenceStep {

                override fun clearAllBuzz(token: Token): Call<Boolean> {
                    assertThat(token, "token").isEqualTo(store.token.value)
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

    private fun `lowerAllHands() returns`(
        participantId: ParticipantId,
        parentParticipantId: ParticipantId?,
    ) = runTest {
        val roster = RosterImpl(
            participantId = participantId,
            parentParticipantId = parentParticipantId,
            step = object : InfinityService.ConferenceStep {

                override fun clearAllBuzz(token: Token): Call<Boolean> {
                    assertThat(token, "token").isEqualTo(store.token.value)
                    return object : TestCall<Boolean> {

                        override fun enqueue(callback: Callback<Boolean>) =
                            callback.onSuccess(this, true)
                    }
                }
            },
        )
        roster.lowerAllHands()
    }

    private fun `lock() throws LockException`(
        participantId: ParticipantId,
        parentParticipantId: ParticipantId?,
    ) = runTest {
        val cause = Throwable()
        val roster = RosterImpl(
            participantId = participantId,
            parentParticipantId = parentParticipantId,
            step = object : InfinityService.ConferenceStep {

                override fun lock(token: Token): Call<Boolean> {
                    assertThat(token, "token").isEqualTo(store.token.value)
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

    private fun `lock() returns`(
        participantId: ParticipantId,
        parentParticipantId: ParticipantId?,
    ) = runTest {
        val roster = RosterImpl(
            participantId = participantId,
            parentParticipantId = parentParticipantId,
            step = object : InfinityService.ConferenceStep {

                override fun lock(token: Token): Call<Boolean> {
                    assertThat(token, "token").isEqualTo(store.token.value)
                    return object : TestCall<Boolean> {

                        override fun enqueue(callback: Callback<Boolean>) =
                            callback.onSuccess(this, true)
                    }
                }
            },
        )
        roster.lock()
    }

    private fun `unlock() throws UnlockException`(
        participantId: ParticipantId,
        parentParticipantId: ParticipantId?,
    ) = runTest {
        val cause = Throwable()
        val roster = RosterImpl(
            participantId = participantId,
            parentParticipantId = parentParticipantId,
            step = object : InfinityService.ConferenceStep {

                override fun unlock(token: Token): Call<Boolean> {
                    assertThat(token, "token").isEqualTo(store.token.value)
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

    private fun `unlock() returns`(
        participantId: ParticipantId,
        parentParticipantId: ParticipantId?,
    ) = runTest {
        val roster = RosterImpl(
            participantId = participantId,
            parentParticipantId = parentParticipantId,
            step = object : InfinityService.ConferenceStep {

                override fun unlock(token: Token): Call<Boolean> {
                    assertThat(token, "token").isEqualTo(store.token.value)
                    return object : TestCall<Boolean> {

                        override fun enqueue(callback: Callback<Boolean>) =
                            callback.onSuccess(this, true)
                    }
                }
            },
        )
        roster.unlock()
    }

    private fun `muteAllGuests() throws MuteAllGuestsException`(
        participantId: ParticipantId,
        parentParticipantId: ParticipantId?,
    ) = runTest {
        val cause = Throwable()
        val roster = RosterImpl(
            participantId = participantId,
            parentParticipantId = parentParticipantId,
            step = object : InfinityService.ConferenceStep {

                override fun muteGuests(token: Token): Call<Boolean> {
                    assertThat(token, "token").isEqualTo(store.token.value)
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

    private fun `muteAllGuests() returns`(
        participantId: ParticipantId,
        parentParticipantId: ParticipantId?,
    ) = runTest {
        val roster = RosterImpl(
            participantId = participantId,
            parentParticipantId = parentParticipantId,
            step = object : InfinityService.ConferenceStep {

                override fun muteGuests(token: Token): Call<Boolean> {
                    assertThat(token, "token").isEqualTo(store.token.value)
                    return object : TestCall<Boolean> {

                        override fun enqueue(callback: Callback<Boolean>) =
                            callback.onSuccess(this, true)
                    }
                }
            },
        )
        roster.muteAllGuests()
    }

    private fun `unmuteAllGuests() throws UnmuteAllGuestsException`(
        participantId: ParticipantId,
        parentParticipantId: ParticipantId?,
    ) = runTest {
        val cause = Throwable()
        val roster = RosterImpl(
            participantId = participantId,
            parentParticipantId = parentParticipantId,
            step = object : InfinityService.ConferenceStep {

                override fun unmuteGuests(token: Token): Call<Boolean> {
                    assertThat(token, "token").isEqualTo(store.token.value)
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

    private fun `unmuteAllGuests() returns`(
        participantId: ParticipantId,
        parentParticipantId: ParticipantId?,
    ) = runTest {
        val roster = RosterImpl(
            participantId = participantId,
            parentParticipantId = parentParticipantId,
            step = object : InfinityService.ConferenceStep {

                override fun unmuteGuests(token: Token): Call<Boolean> {
                    assertThat(token, "token").isEqualTo(store.token.value)
                    return object : TestCall<Boolean> {

                        override fun enqueue(callback: Callback<Boolean>) =
                            callback.onSuccess(this, true)
                    }
                }
            },
        )
        roster.unmuteAllGuests()
    }

    private fun `disconnectAll() throws DisconnectAllException`(
        participantId: ParticipantId,
        parentParticipantId: ParticipantId?,
    ) = runTest {
        val cause = Throwable()
        val roster = RosterImpl(
            participantId = participantId,
            parentParticipantId = parentParticipantId,
            step = object : InfinityService.ConferenceStep {

                override fun disconnect(token: Token): Call<Boolean> {
                    assertThat(token, "token").isEqualTo(store.token.value)
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

    private fun `disconnectAll() returns`(
        participantId: ParticipantId,
        parentParticipantId: ParticipantId?,
    ) = runTest {
        val roster = RosterImpl(
            participantId = participantId,
            parentParticipantId = parentParticipantId,
            step = object : InfinityService.ConferenceStep {

                override fun disconnect(token: Token): Call<Boolean> {
                    assertThat(token, "token").isEqualTo(store.token.value)
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
        id: ParticipantId = nextParticipantId(),
        me: Boolean = false,
    ): Participant {
        val startTime = Instant.fromEpochSeconds(0) + nextInt(0, 100).seconds
        return Participant(
            id = id,
            startTime = startTime,
            role = Role.entries.random(this),
            serviceType = ServiceType.entries.random(this),
            buzzTime = startTime + nextInt(0, 100).seconds,
            spotlightTime = startTime + nextInt(0, 100).seconds,
            displayName = nextString(),
            overlayText = nextString(),
            me = me,
            audioMuted = nextBoolean(),
            videoMuted = nextBoolean(),
            presenting = nextBoolean(),
            muteSupported = nextBoolean(),
            transferSupported = nextBoolean(),
            disconnectSupported = nextBoolean(),
            callTag = nextString(),
        )
    }

    private fun Random.nextParticipantList(
        participantId: ParticipantId,
        parentParticipantId: ParticipantId?,
    ) = List(10) {
        nextParticipant(
            id = if (it == 0) parentParticipantId ?: participantId else nextParticipantId(),
            me = it == 0,
        )
    }

    private fun List<Participant>.toParticipantIdSet(participantId: ParticipantId) = buildSet {
        add(participantId)
        for (participant in this@toParticipantIdSet) add(participant.id)
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
            Role.HOST -> Role.HOST
            Role.GUEST -> Role.GUEST
            Role.UNKNOWN -> Role.UNKNOWN
        },
        serviceType = when (serviceType) {
            ServiceType.CONNECTING -> ServiceType.CONNECTING
            ServiceType.WAITING_ROOM -> ServiceType.WAITING_ROOM
            ServiceType.IVR -> ServiceType.IVR
            ServiceType.CONFERENCE -> ServiceType.CONFERENCE
            ServiceType.LECTURE -> ServiceType.LECTURE
            ServiceType.GATEWAY -> ServiceType.GATEWAY
            ServiceType.TEST_CALL -> ServiceType.TEST_CALL
            ServiceType.UNKNOWN -> ServiceType.UNKNOWN
        },
        callTag = callTag,
    )

    @Suppress("TestFunctionName")
    private fun TestScope.RosterImpl(
        participantId: ParticipantId,
        parentParticipantId: ParticipantId?,
        step: InfinityService.ConferenceStep = object : InfinityService.ConferenceStep {
            override fun participant(participantId: ParticipantId): InfinityService.ParticipantStep =
                object : InfinityService.ParticipantStep {}
        },
    ) = RosterImpl(
        scope = backgroundScope,
        event = event,
        participantId = participantId,
        parentParticipantId = parentParticipantId,
        store = store,
        step = step,
    )
}
