/*
 * Copyright 2023-2025 Pexip AS
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
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.isTrue
import assertk.fail
import assertk.tableOf
import com.pexip.sdk.api.Call
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
import com.pexip.sdk.api.infinity.SetGuestCanUnmuteRequest
import com.pexip.sdk.api.infinity.SpeakerResponse
import com.pexip.sdk.api.infinity.StageEvent
import com.pexip.sdk.api.infinity.Token
import com.pexip.sdk.api.infinity.TokenStore
import com.pexip.sdk.conference.AdmitException
import com.pexip.sdk.conference.AllowGuestsToUnmuteException
import com.pexip.sdk.conference.ClientMuteException
import com.pexip.sdk.conference.ClientUnmuteException
import com.pexip.sdk.conference.DisallowGuestsToUnmuteException
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
import com.pexip.sdk.conference.Roster
import com.pexip.sdk.conference.SpotlightException
import com.pexip.sdk.conference.UnlockException
import com.pexip.sdk.conference.UnmuteAllGuestsException
import com.pexip.sdk.conference.UnmuteException
import com.pexip.sdk.conference.UnmuteVideoException
import com.pexip.sdk.conference.UnspotlightException
import com.pexip.sdk.infinity.ParticipantId
import com.pexip.sdk.infinity.Role
import com.pexip.sdk.infinity.ServiceType
import com.pexip.sdk.infinity.VersionId
import com.pexip.sdk.infinity.test.nextParticipantId
import com.pexip.sdk.infinity.test.nextString
import kotlinx.coroutines.CompletableDeferred
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
    fun `guestsCanUnmute produces the correct guests can unmute state`() {
        table.forAll(::`guestsCanUnmute produces the correct guests can unmute state`)
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
    fun `raiseHand(null) returns`() {
        table.forAll { participantId, parentParticipantId ->
            `raiseHand(null) returns`(VersionId.V35, participantId, parentParticipantId)
        }
    }

    @Test
    fun `raiseHand(null) returns on V35_1+`() {
        table.forAll { participantId, parentParticipantId ->
            `raiseHand(null) returns`(VersionId.V35_1, participantId, parentParticipantId)
        }
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
    fun `disconnect(null) returns`() {
        table.forAll { participantId, parentParticipantId ->
            `disconnect(null) returns`(VersionId.V35, participantId, parentParticipantId)
        }
    }

    @Test
    fun `disconnect(null) returns on V35_1+`() {
        table.forAll { participantId, parentParticipantId ->
            `disconnect(null) returns`(VersionId.V35_1, participantId, parentParticipantId)
        }
    }

    @Test
    fun `makeHost() throws MakeHostException`() {
        table.forAll(::`makeHost() throws MakeHostException`)
    }

    @Test
    fun `makeHost() returns`() {
        table.forAll(::`makeHost() returns`)
    }

    @Test
    fun `makeHost(null) returns`() {
        table.forAll { participantId, parentParticipantId ->
            `makeHost(null) returns`(VersionId.V35, participantId, parentParticipantId)
        }
    }

    @Test
    fun `makeHost(null) returns on V35_1+`() {
        table.forAll { participantId, parentParticipantId ->
            `makeHost(null) returns`(VersionId.V35_1, participantId, parentParticipantId)
        }
    }

    @Test
    fun `makeGuest() throws MakeGuestException`() {
        table.forAll(::`makeGuest() throws MakeGuestException`)
    }

    @Test
    fun `makeGuest() returns`() {
        table.forAll(::`makeGuest() returns`)
    }

    @Test
    fun `makeGuest(null) returns`() {
        table.forAll { participantId, parentParticipantId ->
            `makeGuest(null) returns`(VersionId.V35, participantId, parentParticipantId)
        }
    }

    @Test
    fun `makeGuest(null) returns on V35_1+`() {
        table.forAll { participantId, parentParticipantId ->
            `makeGuest(null) returns`(VersionId.V35_1, participantId, parentParticipantId)
        }
    }

    @Test
    fun `clientMute() throws ClientMuteException`() {
        table.forAll { participantId, parentParticipantId ->
            `clientMute() throws ClientMuteException`(
                versionId = VersionId.V35,
                serviceType = ServiceType.CONFERENCE,
                participantId = participantId,
                parentParticipantId = parentParticipantId,
            )
        }
    }

    @Test
    fun `clientMute() throws ClientMuteException on V36+`() {
        table.forAll { participantId, parentParticipantId ->
            `clientMute() throws ClientMuteException`(
                versionId = VersionId.V36,
                serviceType = ServiceType.CONFERENCE,
                participantId = participantId,
                parentParticipantId = parentParticipantId,
            )
        }
    }

    @Test
    fun `clientMute() throws ClientMuteException in GATEWAY`() {
        table.forAll { participantId, parentParticipantId ->
            `clientMute() throws ClientMuteException`(
                versionId = VersionId.V36,
                serviceType = ServiceType.GATEWAY,
                participantId = participantId,
                parentParticipantId = parentParticipantId,
            )
        }
    }

    @Test
    fun `clientMute() returns`() {
        table.forAll { participantId, parentParticipantId ->
            `clientMute() returns`(
                versionId = VersionId.V35,
                serviceType = ServiceType.CONFERENCE,
                participantId = participantId,
                parentParticipantId = parentParticipantId,
            )
        }
    }

    @Test
    fun `clientMute() returns on V36+`() {
        table.forAll { participantId, parentParticipantId ->
            `clientMute() returns`(
                versionId = VersionId.V36,
                serviceType = ServiceType.CONFERENCE,
                participantId = participantId,
                parentParticipantId = parentParticipantId,
            )
        }
    }

    @Test
    fun `clientMute() returns in GATEWAY`() {
        table.forAll { participantId, parentParticipantId ->
            `clientMute() returns`(
                versionId = VersionId.V36,
                serviceType = ServiceType.GATEWAY,
                participantId = participantId,
                parentParticipantId = parentParticipantId,
            )
        }
    }

    @Test
    fun `clientUnmute() throws ClientUnmuteException`() {
        table.forAll { participantId, parentParticipantId ->
            `clientUnmute() throws ClientUnmuteException`(
                versionId = VersionId.V35,
                serviceType = ServiceType.CONFERENCE,
                participantId = participantId,
                parentParticipantId = parentParticipantId,
            )
        }
    }

    @Test
    fun `clientUnmute() throws ClientUnmuteException on V36+`() {
        table.forAll { participantId, parentParticipantId ->
            `clientUnmute() throws ClientUnmuteException`(
                versionId = VersionId.V36,
                serviceType = ServiceType.CONFERENCE,
                participantId = participantId,
                parentParticipantId = parentParticipantId,
            )
        }
    }

    @Test
    fun `clientUnmute() throws ClientUnmuteException in GATEWAY`() {
        table.forAll { participantId, parentParticipantId ->
            `clientUnmute() throws ClientUnmuteException`(
                versionId = VersionId.V36,
                serviceType = ServiceType.GATEWAY,
                participantId = participantId,
                parentParticipantId = parentParticipantId,
            )
        }
    }

    @Test
    fun `clientUnmute() returns`() {
        table.forAll { participantId, parentParticipantId ->
            `clientUnmute() returns`(
                versionId = VersionId.V35,
                serviceType = ServiceType.CONFERENCE,
                participantId = participantId,
                parentParticipantId = parentParticipantId,
            )
        }
    }

    @Test
    fun `clientUnmute() returns on V36+`() {
        table.forAll { participantId, parentParticipantId ->
            `clientUnmute() returns`(
                versionId = VersionId.V36,
                serviceType = ServiceType.CONFERENCE,
                participantId = participantId,
                parentParticipantId = parentParticipantId,
            )
        }
    }

    @Test
    fun `clientUnmute() returns in GATEWAY`() {
        table.forAll { participantId, parentParticipantId ->
            `clientUnmute() returns`(
                versionId = VersionId.V36,
                serviceType = ServiceType.GATEWAY,
                participantId = participantId,
                parentParticipantId = parentParticipantId,
            )
        }
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
    fun `mute(null) returns`() {
        table.forAll(::`mute(null) returns`)
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
    fun `unmute(null) returns`() {
        table.forAll(::`unmute(null) returns`)
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
    fun `muteVideo(null) returns`() {
        table.forAll { participantId, parentParticipantId ->
            `muteVideo(null) returns`(VersionId.V35, participantId, parentParticipantId)
        }
    }

    @Test
    fun `muteVideo(null) returns on V35_1+`() {
        table.forAll { participantId, parentParticipantId ->
            `muteVideo(null) returns`(VersionId.V35_1, participantId, parentParticipantId)
        }
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
    fun `unmuteVideo(null) returns`() {
        table.forAll { participantId, parentParticipantId ->
            `unmuteVideo(null) returns`(VersionId.V35, participantId, parentParticipantId)
        }
    }

    @Test
    fun `unmuteVideo(null) returns on V35_1+`() {
        table.forAll { participantId, parentParticipantId ->
            `unmuteVideo(null) returns`(VersionId.V35_1, participantId, parentParticipantId)
        }
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
    fun `spotlight(null) returns`() {
        table.forAll { participantId, parentParticipantId ->
            `spotlight(null) returns`(VersionId.V35, participantId, parentParticipantId)
        }
    }

    @Test
    fun `spotlight(null) returns on V35_1+`() {
        table.forAll { participantId, parentParticipantId ->
            `spotlight(null) returns`(VersionId.V35_1, participantId, parentParticipantId)
        }
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
    fun `unspotlight(null) returns`() {
        table.forAll { participantId, parentParticipantId ->
            `unspotlight(null) returns`(VersionId.V35, participantId, parentParticipantId)
        }
    }

    @Test
    fun `unspotlight(null) returns on V35_1+`() {
        table.forAll { participantId, parentParticipantId ->
            `unspotlight(null) returns`(VersionId.V35_1, participantId, parentParticipantId)
        }
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
    fun `lowerHand(null) returns`() {
        table.forAll { participantId, parentParticipantId ->
            `lowerHand(null) returns`(VersionId.V35, participantId, parentParticipantId)
        }
    }

    @Test
    fun `lowerHand(null) returns on V35_1+`() {
        table.forAll { participantId, parentParticipantId ->
            `lowerHand(null) returns`(VersionId.V35_1, participantId, parentParticipantId)
        }
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
    fun `allowGuestsToUnmute() throws AllowGuestsToUnmuteException`() {
        table.forAll(::`allowGuestsToUnmute() throws AllowGuestsToUnmuteException`)
    }

    @Test
    fun `allowGuestsToUnmute() returns`() {
        table.forAll(::`allowGuestsToUnmute() returns`)
    }

    @Test
    fun `disallowGuestsToUnmute() throws DisallowGuestsToUnmuteException`() {
        table.forAll(::`disallowGuestsToUnmute() throws DisallowGuestsToUnmuteException`)
    }

    @Test
    fun `disallowGuestsToUnmute() returns`() {
        table.forAll(::`disallowGuestsToUnmute() returns`)
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

    private fun `guestsCanUnmute produces the correct guests can unmute state`(
        participantId: ParticipantId,
        parentParticipantId: ParticipantId?,
    ) = runTest {
        val roster = RosterImpl(participantId, parentParticipantId)
        roster.guestsCanUnmute.test {
            event.subscriptionCount.first { it > 0 }
            assertThat(awaitItem(), "guestsMuted").isNull()
            event.emit(ConferenceUpdateEvent(guestsCanUnmute = true))
            assertThat(awaitItem(), "guestsMuted")
                .isNotNull()
                .isTrue()
            event.emit(ConferenceUpdateEvent(guestsCanUnmute = true))
            expectNoEvents()
            event.emit(ConferenceUpdateEvent(guestsCanUnmute = false))
            assertThat(awaitItem(), "guestsMuted")
                .isNotNull()
                .isFalse()
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

                override fun participant(
                    participantId: ParticipantId,
                ): InfinityService.ParticipantStep {
                    assertThat(participantId, "participantId").isIn(*participantIds.toTypedArray())
                    return object : InfinityService.ParticipantStep {

                        override fun buzz(token: Token): Call<Boolean> {
                            assertThat(token, "token").isEqualTo(store.token.value)
                            return call { throw causes.getValue(participantId) }
                        }
                    }
                }
            },
        )
        roster.populate(participants)
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

                override fun participant(
                    participantId: ParticipantId,
                ): InfinityService.ParticipantStep {
                    assertThat(participantId, "participantId").isIn(*participantIds.toTypedArray())
                    return object : InfinityService.ParticipantStep {

                        override fun buzz(token: Token): Call<Boolean> {
                            assertThat(token, "token").isEqualTo(store.token.value)
                            return call { true }
                        }
                    }
                }
            },
        )
        roster.populate(participants)
        participants.forEach { roster.raiseHand(it.id) }
    }

    private fun `raiseHand(null) returns`(
        versionId: VersionId,
        participantId: ParticipantId,
        parentParticipantId: ParticipantId?,
    ) = runTest {
        val deferredParticipantId = CompletableDeferred<ParticipantId>()
        val participants = Random.nextParticipantList(participantId, parentParticipantId)
        val roster = RosterImpl(
            versionId = versionId,
            participantId = participantId,
            parentParticipantId = parentParticipantId,
            step = object : InfinityService.ConferenceStep {

                override fun participant(
                    participantId: ParticipantId,
                ): InfinityService.ParticipantStep = object : InfinityService.ParticipantStep {

                    override fun buzz(token: Token): Call<Boolean> {
                        assertThat(token, "token").isEqualTo(store.token.value)
                        return call {
                            deferredParticipantId.complete(participantId)
                            true
                        }
                    }
                }
            },
        )
        roster.populate(participants)
        roster.raiseHand()
        val expected = when (versionId < VersionId.V35_1) {
            true -> participantId
            else -> parentParticipantId ?: participantId
        }
        assertThat(deferredParticipantId.await(), "participantId").isEqualTo(expected)
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

                override fun participant(
                    participantId: ParticipantId,
                ): InfinityService.ParticipantStep {
                    assertThat(participantId, "participantId").isIn(*participantIds.toTypedArray())
                    return object : InfinityService.ParticipantStep {

                        override fun unlock(token: Token): Call<Boolean> {
                            assertThat(token, "token").isEqualTo(store.token.value)
                            return call { throw causes.getValue(participantId) }
                        }
                    }
                }
            },
        )
        roster.populate(participants)
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

                override fun participant(
                    participantId: ParticipantId,
                ): InfinityService.ParticipantStep {
                    assertThat(participantId, "participantId").isIn(*participantIds.toTypedArray())
                    return object : InfinityService.ParticipantStep {

                        override fun unlock(token: Token): Call<Boolean> {
                            assertThat(token, "token").isEqualTo(store.token.value)
                            return call { true }
                        }
                    }
                }
            },
        )
        roster.populate(participants)
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

                override fun participant(
                    participantId: ParticipantId,
                ): InfinityService.ParticipantStep {
                    assertThat(participantId, "participantId").isIn(*participantIds.toTypedArray())
                    return object : InfinityService.ParticipantStep {

                        override fun disconnect(token: Token): Call<Boolean> {
                            assertThat(token, "token").isEqualTo(store.token.value)
                            return call { throw causes.getValue(participantId) }
                        }
                    }
                }
            },
        )
        roster.populate(participants)
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

                override fun participant(
                    participantId: ParticipantId,
                ): InfinityService.ParticipantStep {
                    assertThat(participantId, "participantId").isIn(*participantIds.toTypedArray())
                    return object : InfinityService.ParticipantStep {

                        override fun disconnect(token: Token): Call<Boolean> {
                            assertThat(token, "token").isEqualTo(store.token.value)
                            return call { true }
                        }
                    }
                }
            },
        )
        roster.populate(participants)
        participants.forEach { roster.disconnect(it.id) }
    }

    private fun `disconnect(null) returns`(
        versionId: VersionId,
        participantId: ParticipantId,
        parentParticipantId: ParticipantId?,
    ) = runTest {
        val deferredParticipantId = CompletableDeferred<ParticipantId>()
        val participants = Random.nextParticipantList(participantId, parentParticipantId)
        val roster = RosterImpl(
            versionId = versionId,
            participantId = participantId,
            parentParticipantId = parentParticipantId,
            step = object : InfinityService.ConferenceStep {

                override fun participant(
                    participantId: ParticipantId,
                ): InfinityService.ParticipantStep = object : InfinityService.ParticipantStep {

                    override fun disconnect(token: Token): Call<Boolean> {
                        assertThat(token, "token").isEqualTo(store.token.value)
                        return call {
                            deferredParticipantId.complete(participantId)
                            true
                        }
                    }
                }
            },
        )
        roster.populate(participants)
        roster.disconnect()
        val expected = when (versionId < VersionId.V35_1) {
            true -> participantId
            else -> parentParticipantId ?: participantId
        }
        assertThat(deferredParticipantId.await(), "participantId").isEqualTo(expected)
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

                override fun participant(
                    participantId: ParticipantId,
                ): InfinityService.ParticipantStep {
                    assertThat(participantId, "participantId").isIn(*participantIds.toTypedArray())
                    return object : InfinityService.ParticipantStep {

                        override fun role(request: RoleRequest, token: Token): Call<Boolean> {
                            assertThat(token, "token").isEqualTo(store.token.value)
                            assertThat(request::role, "request").isEqualTo(Role.HOST)
                            return call { throw causes.getValue(participantId) }
                        }
                    }
                }
            },
        )
        roster.populate(participants)
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

                override fun participant(
                    participantId: ParticipantId,
                ): InfinityService.ParticipantStep {
                    assertThat(participantId, "participantId").isIn(*participantIds.toTypedArray())
                    return object : InfinityService.ParticipantStep {

                        override fun role(request: RoleRequest, token: Token): Call<Boolean> {
                            assertThat(token, "token").isEqualTo(store.token.value)
                            assertThat(request::role).isEqualTo(Role.HOST)
                            return call { true }
                        }
                    }
                }
            },
        )
        roster.populate(participants)
        participants.forEach { roster.makeHost(it.id) }
    }

    private fun `makeHost(null) returns`(
        versionId: VersionId,
        participantId: ParticipantId,
        parentParticipantId: ParticipantId?,
    ) = runTest {
        val deferredParticipantId = CompletableDeferred<ParticipantId>()
        val participants = Random.nextParticipantList(participantId, parentParticipantId)
        val roster = RosterImpl(
            versionId = versionId,
            participantId = participantId,
            parentParticipantId = parentParticipantId,
            step = object : InfinityService.ConferenceStep {

                override fun participant(
                    participantId: ParticipantId,
                ): InfinityService.ParticipantStep = object : InfinityService.ParticipantStep {

                    override fun role(request: RoleRequest, token: Token): Call<Boolean> {
                        assertThat(token, "token").isEqualTo(store.token.value)
                        assertThat(request::role).isEqualTo(Role.HOST)
                        return call {
                            deferredParticipantId.complete(participantId)
                            true
                        }
                    }
                }
            },
        )
        roster.populate(participants)
        roster.makeHost()
        val expected = when (versionId < VersionId.V35_1) {
            true -> participantId
            else -> parentParticipantId ?: participantId
        }
        assertThat(deferredParticipantId.await(), "participantId").isEqualTo(expected)
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

                override fun participant(
                    participantId: ParticipantId,
                ): InfinityService.ParticipantStep {
                    assertThat(participantId, "participantId").isIn(*participantIds.toTypedArray())
                    return object : InfinityService.ParticipantStep {

                        override fun role(request: RoleRequest, token: Token): Call<Boolean> {
                            assertThat(token, "token").isEqualTo(store.token.value)
                            assertThat(request::role, "request").isEqualTo(Role.GUEST)
                            return call { throw causes.getValue(participantId) }
                        }
                    }
                }
            },
        )
        roster.populate(participants)
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

                override fun participant(
                    participantId: ParticipantId,
                ): InfinityService.ParticipantStep {
                    assertThat(participantId, "participantId").isIn(*participantIds.toTypedArray())
                    return object : InfinityService.ParticipantStep {

                        override fun role(request: RoleRequest, token: Token): Call<Boolean> {
                            assertThat(token, "token").isEqualTo(store.token.value)
                            assertThat(request::role).isEqualTo(Role.GUEST)
                            return call { true }
                        }
                    }
                }
            },
        )
        roster.populate(participants)
        participants.forEach { roster.makeGuest(it.id) }
    }

    private fun `makeGuest(null) returns`(
        versionId: VersionId,
        participantId: ParticipantId,
        parentParticipantId: ParticipantId?,
    ) = runTest {
        val deferredParticipantId = CompletableDeferred<ParticipantId>()
        val participants = Random.nextParticipantList(participantId, parentParticipantId)
        val roster = RosterImpl(
            versionId = versionId,
            participantId = participantId,
            parentParticipantId = parentParticipantId,
            step = object : InfinityService.ConferenceStep {

                override fun participant(
                    participantId: ParticipantId,
                ): InfinityService.ParticipantStep = object : InfinityService.ParticipantStep {

                    override fun role(request: RoleRequest, token: Token): Call<Boolean> {
                        assertThat(token, "token").isEqualTo(store.token.value)
                        assertThat(request::role).isEqualTo(Role.GUEST)
                        return call {
                            deferredParticipantId.complete(participantId)
                            true
                        }
                    }
                }
            },
        )
        roster.populate(participants)
        roster.makeGuest()
        val expected = when (versionId < VersionId.V35_1) {
            true -> participantId
            else -> parentParticipantId ?: participantId
        }
        assertThat(deferredParticipantId.await(), "participantId").isEqualTo(expected)
    }

    private fun `clientMute() throws ClientMuteException`(
        versionId: VersionId,
        serviceType: ServiceType,
        participantId: ParticipantId,
        parentParticipantId: ParticipantId?,
    ) = runTest {
        val deferredParticipantId = CompletableDeferred<ParticipantId>()
        val participants = Random.nextParticipantList(participantId, parentParticipantId)
        val cause = Throwable()
        val roster = RosterImpl(
            versionId = versionId,
            serviceType = serviceType,
            participantId = participantId,
            parentParticipantId = parentParticipantId,
            step = object : InfinityService.ConferenceStep {

                override fun participant(
                    participantId: ParticipantId,
                ): InfinityService.ParticipantStep = object : InfinityService.ParticipantStep {

                    override fun mute(token: Token): Call<Unit> {
                        if (versionId >= VersionId.V36 && serviceType != ServiceType.GATEWAY) {
                            fail("Should not be called.")
                        }
                        assertThat(token, "token").isEqualTo(store.token.value)
                        return call {
                            deferredParticipantId.complete(participantId)
                            throw cause
                        }
                    }

                    override fun clientMute(token: Token): Call<Unit> {
                        if (serviceType == ServiceType.GATEWAY) fail("Should not be called.")
                        if (versionId < VersionId.V36) fail("Should not be called.")
                        assertThat(token, "token").isEqualTo(store.token.value)
                        return call {
                            deferredParticipantId.complete(participantId)
                            throw cause
                        }
                    }
                }
            },
        )
        roster.populate(participants)
        assertFailure { roster.clientMute() }
            .isInstanceOf<ClientMuteException>()
            .hasCause(cause)
        assertThat(deferredParticipantId.await(), "participantId")
            .isEqualTo(parentParticipantId ?: participantId)
    }

    private fun `clientMute() returns`(
        versionId: VersionId,
        serviceType: ServiceType,
        participantId: ParticipantId,
        parentParticipantId: ParticipantId?,
    ) = runTest {
        val deferredParticipantId = CompletableDeferred<ParticipantId>()
        val participants = Random.nextParticipantList(participantId, parentParticipantId)
        val roster = RosterImpl(
            versionId = versionId,
            serviceType = serviceType,
            participantId = participantId,
            parentParticipantId = parentParticipantId,
            step = object : InfinityService.ConferenceStep {

                override fun participant(
                    participantId: ParticipantId,
                ): InfinityService.ParticipantStep = object : InfinityService.ParticipantStep {

                    override fun mute(token: Token): Call<Unit> {
                        if (versionId >= VersionId.V36 && serviceType != ServiceType.GATEWAY) {
                            fail("Should not be called.")
                        }
                        assertThat(token, "token").isEqualTo(store.token.value)
                        return call { deferredParticipantId.complete(participantId) }
                    }

                    override fun clientMute(token: Token): Call<Unit> {
                        if (serviceType == ServiceType.GATEWAY) fail("Should not be called.")
                        if (versionId < VersionId.V36) fail("Should not be called.")
                        assertThat(token, "token").isEqualTo(store.token.value)
                        return call { deferredParticipantId.complete(participantId) }
                    }
                }
            },
        )
        roster.populate(participants)
        roster.clientMute()
        assertThat(deferredParticipantId.await(), "participantId")
            .isEqualTo(parentParticipantId ?: participantId)
    }

    private fun `clientUnmute() throws ClientUnmuteException`(
        versionId: VersionId,
        serviceType: ServiceType,
        participantId: ParticipantId,
        parentParticipantId: ParticipantId?,
    ) = runTest {
        val deferredParticipantId = CompletableDeferred<ParticipantId>()
        val participants = Random.nextParticipantList(participantId, parentParticipantId)
        val cause = Throwable()
        val roster = RosterImpl(
            versionId = versionId,
            serviceType = serviceType,
            participantId = participantId,
            parentParticipantId = parentParticipantId,
            step = object : InfinityService.ConferenceStep {

                override fun participant(
                    participantId: ParticipantId,
                ): InfinityService.ParticipantStep = object : InfinityService.ParticipantStep {

                    override fun unmute(token: Token): Call<Unit> {
                        assertThat(token, "token").isEqualTo(store.token.value)
                        return call {
                            deferredParticipantId.complete(participantId)
                            throw cause
                        }
                    }

                    override fun clientUnmute(token: Token): Call<Unit> {
                        if (serviceType == ServiceType.GATEWAY) fail("Should not be called.")
                        if (versionId < VersionId.V36) fail("Should not be called.")
                        assertThat(token, "token").isEqualTo(store.token.value)
                        return call {
                            deferredParticipantId.complete(participantId)
                            throw cause
                        }
                    }
                }
            },
        )
        roster.populate(participants)
        assertFailure { roster.clientUnmute() }
            .isInstanceOf<ClientUnmuteException>()
            .hasCause(cause)
        assertThat(deferredParticipantId.await(), "participantId")
            .isEqualTo(parentParticipantId ?: participantId)
    }

    private fun `clientUnmute() returns`(
        versionId: VersionId,
        serviceType: ServiceType,
        participantId: ParticipantId,
        parentParticipantId: ParticipantId?,
    ) = runTest {
        val unmuteParticipantId = CompletableDeferred<ParticipantId>()
        val clientUnmuteParticipantId = CompletableDeferred<ParticipantId>()
        val participants = Random.nextParticipantList(participantId, parentParticipantId)
        val roster = RosterImpl(
            versionId = versionId,
            serviceType = serviceType,
            participantId = participantId,
            parentParticipantId = parentParticipantId,
            step = object : InfinityService.ConferenceStep {

                override fun participant(
                    participantId: ParticipantId,
                ): InfinityService.ParticipantStep = object : InfinityService.ParticipantStep {

                    override fun unmute(token: Token): Call<Unit> {
                        assertThat(token, "token").isEqualTo(store.token.value)
                        return call { unmuteParticipantId.complete(participantId) }
                    }

                    override fun clientUnmute(token: Token): Call<Unit> {
                        if (serviceType == ServiceType.GATEWAY) fail("Should not be called.")
                        if (versionId < VersionId.V36) fail("Should not be called.")
                        assertThat(token, "token").isEqualTo(store.token.value)
                        return call { clientUnmuteParticipantId.complete(participantId) }
                    }
                }
            },
        )
        roster.populate(participants)
        roster.clientUnmute()
        assertThat(unmuteParticipantId.await(), "unmuteParticipantId")
            .isEqualTo(parentParticipantId ?: participantId)
        if (serviceType != ServiceType.GATEWAY && versionId >= VersionId.V36) {
            assertThat(clientUnmuteParticipantId.await(), "clientUnmuteParticipantId")
                .isEqualTo(parentParticipantId ?: participantId)
        }
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

                override fun participant(
                    participantId: ParticipantId,
                ): InfinityService.ParticipantStep {
                    assertThat(participantId, "participantId").isIn(*participantIds.toTypedArray())
                    return object : InfinityService.ParticipantStep {

                        override fun mute(token: Token): Call<Unit> {
                            assertThat(token, "token").isEqualTo(store.token.value)
                            return call { throw causes.getValue(participantId) }
                        }
                    }
                }
            },
        )
        roster.populate(participants)
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

                override fun participant(
                    participantId: ParticipantId,
                ): InfinityService.ParticipantStep {
                    assertThat(participantId, "participantId").isIn(*participantIds.toTypedArray())
                    return object : InfinityService.ParticipantStep {

                        override fun mute(token: Token): Call<Unit> {
                            assertThat(token, "token").isEqualTo(store.token.value)
                            return call { }
                        }
                    }
                }
            },
        )
        roster.populate(participants)
        participants.forEach { roster.mute(it.id) }
    }

    private fun `mute(null) returns`(
        participantId: ParticipantId,
        parentParticipantId: ParticipantId?,
    ) = runTest {
        val deferredParticipantId = CompletableDeferred<ParticipantId>()
        val participants = Random.nextParticipantList(participantId, parentParticipantId)
        val roster = RosterImpl(
            participantId = participantId,
            parentParticipantId = parentParticipantId,
            step = object : InfinityService.ConferenceStep {

                override fun participant(
                    participantId: ParticipantId,
                ): InfinityService.ParticipantStep = object : InfinityService.ParticipantStep {

                    override fun mute(token: Token): Call<Unit> {
                        assertThat(token, "token").isEqualTo(store.token.value)
                        return call { deferredParticipantId.complete(participantId) }
                    }
                }
            },
        )
        roster.populate(participants)
        roster.mute()
        assertThat(deferredParticipantId.await(), "participantId")
            .isEqualTo(parentParticipantId ?: participantId)
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

                override fun participant(
                    participantId: ParticipantId,
                ): InfinityService.ParticipantStep {
                    assertThat(participantId, "participantId").isIn(*participantIds.toTypedArray())
                    return object : InfinityService.ParticipantStep {

                        override fun unmute(token: Token): Call<Unit> {
                            assertThat(token, "token").isEqualTo(store.token.value)
                            return call { throw causes.getValue(participantId) }
                        }
                    }
                }
            },
        )
        roster.populate(participants)
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

                override fun participant(
                    participantId: ParticipantId,
                ): InfinityService.ParticipantStep {
                    assertThat(participantId, "participantId").isIn(*participantIds.toTypedArray())
                    return object : InfinityService.ParticipantStep {

                        override fun unmute(token: Token): Call<Unit> {
                            assertThat(token, "token").isEqualTo(store.token.value)
                            return call { }
                        }
                    }
                }
            },
        )
        roster.populate(participants)
        participants.forEach { roster.unmute(it.id) }
    }

    private fun `unmute(null) returns`(
        participantId: ParticipantId,
        parentParticipantId: ParticipantId?,
    ) = runTest {
        val deferredParticipantId = CompletableDeferred<ParticipantId>()
        val participants = Random.nextParticipantList(participantId, parentParticipantId)
        val roster = RosterImpl(
            participantId = participantId,
            parentParticipantId = parentParticipantId,
            step = object : InfinityService.ConferenceStep {

                override fun participant(
                    participantId: ParticipantId,
                ): InfinityService.ParticipantStep = object : InfinityService.ParticipantStep {

                    override fun unmute(token: Token): Call<Unit> {
                        assertThat(token, "token").isEqualTo(store.token.value)
                        return call { deferredParticipantId.complete(participantId) }
                    }
                }
            },
        )
        roster.populate(participants)
        roster.unmute()
        assertThat(deferredParticipantId.await(), "participantId")
            .isEqualTo(parentParticipantId ?: participantId)
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

                override fun participant(
                    participantId: ParticipantId,
                ): InfinityService.ParticipantStep {
                    assertThat(participantId, "participantId").isIn(*participantIds.toTypedArray())
                    return object : InfinityService.ParticipantStep {

                        override fun videoMuted(token: Token): Call<Unit> {
                            assertThat(token, "token").isEqualTo(store.token.value)
                            return call { throw causes.getValue(participantId) }
                        }
                    }
                }
            },
        )
        roster.populate(participants)
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

                override fun participant(
                    participantId: ParticipantId,
                ): InfinityService.ParticipantStep {
                    assertThat(participantId, "participantId").isIn(*participantIds.toTypedArray())
                    return object : InfinityService.ParticipantStep {

                        override fun videoMuted(token: Token): Call<Unit> {
                            assertThat(token, "token").isEqualTo(store.token.value)
                            return call { }
                        }
                    }
                }
            },
        )
        roster.populate(participants)
        participants.forEach { roster.muteVideo(it.id) }
    }

    private fun `muteVideo(null) returns`(
        versionId: VersionId,
        participantId: ParticipantId,
        parentParticipantId: ParticipantId?,
    ) = runTest {
        val deferredParticipantId = CompletableDeferred<ParticipantId>()
        val participants = Random.nextParticipantList(participantId, parentParticipantId)
        val roster = RosterImpl(
            versionId = versionId,
            participantId = participantId,
            parentParticipantId = parentParticipantId,
            step = object : InfinityService.ConferenceStep {

                override fun participant(
                    participantId: ParticipantId,
                ): InfinityService.ParticipantStep = object : InfinityService.ParticipantStep {

                    override fun videoMuted(token: Token): Call<Unit> {
                        assertThat(token, "token").isEqualTo(store.token.value)
                        return call { deferredParticipantId.complete(participantId) }
                    }
                }
            },
        )
        roster.populate(participants)
        roster.muteVideo()
        val expected = when (versionId < VersionId.V35_1) {
            true -> participantId
            else -> parentParticipantId ?: participantId
        }
        assertThat(deferredParticipantId.await(), "participantId").isEqualTo(expected)
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

                override fun participant(
                    participantId: ParticipantId,
                ): InfinityService.ParticipantStep {
                    assertThat(participantId, "participantId").isIn(*participantIds.toTypedArray())
                    return object : InfinityService.ParticipantStep {

                        override fun videoUnmuted(token: Token): Call<Unit> {
                            assertThat(token, "token").isEqualTo(store.token.value)
                            return call { throw causes.getValue(participantId) }
                        }
                    }
                }
            },
        )
        roster.populate(participants)
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

                override fun participant(
                    participantId: ParticipantId,
                ): InfinityService.ParticipantStep {
                    assertThat(participantId, "participantId").isIn(*participantIds.toTypedArray())
                    return object : InfinityService.ParticipantStep {

                        override fun videoUnmuted(token: Token): Call<Unit> {
                            assertThat(token, "token").isEqualTo(store.token.value)
                            return call { }
                        }
                    }
                }
            },
        )
        roster.populate(participants)
        participants.forEach { roster.unmuteVideo(it.id) }
    }

    private fun `unmuteVideo(null) returns`(
        versionId: VersionId,
        participantId: ParticipantId,
        parentParticipantId: ParticipantId?,
    ) = runTest {
        val deferredParticipantId = CompletableDeferred<ParticipantId>()
        val participants = Random.nextParticipantList(participantId, parentParticipantId)
        val roster = RosterImpl(
            versionId = versionId,
            participantId = participantId,
            parentParticipantId = parentParticipantId,
            step = object : InfinityService.ConferenceStep {

                override fun participant(
                    participantId: ParticipantId,
                ): InfinityService.ParticipantStep = object : InfinityService.ParticipantStep {

                    override fun videoUnmuted(token: Token): Call<Unit> {
                        assertThat(token, "token").isEqualTo(store.token.value)
                        return call { deferredParticipantId.complete(participantId) }
                    }
                }
            },
        )
        roster.populate(participants)
        roster.unmuteVideo()
        val expected = when (versionId < VersionId.V35_1) {
            true -> participantId
            else -> parentParticipantId ?: participantId
        }
        assertThat(deferredParticipantId.await(), "participantId").isEqualTo(expected)
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

                override fun participant(
                    participantId: ParticipantId,
                ): InfinityService.ParticipantStep {
                    assertThat(participantId, "participantId").isIn(*participantIds.toTypedArray())
                    return object : InfinityService.ParticipantStep {

                        override fun spotlightOn(token: Token): Call<Boolean> {
                            assertThat(token, "token").isEqualTo(store.token.value)
                            return call { throw causes.getValue(participantId) }
                        }
                    }
                }
            },
        )
        roster.populate(participants)
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

                override fun participant(
                    participantId: ParticipantId,
                ): InfinityService.ParticipantStep {
                    assertThat(participantId, "participantId").isIn(*participantIds.toTypedArray())
                    return object : InfinityService.ParticipantStep {

                        override fun spotlightOn(token: Token): Call<Boolean> {
                            assertThat(token, "token").isEqualTo(store.token.value)
                            return call { true }
                        }
                    }
                }
            },
        )
        roster.populate(participants)
        participants.forEach { roster.spotlight(it.id) }
    }

    private fun `spotlight(null) returns`(
        versionId: VersionId,
        participantId: ParticipantId,
        parentParticipantId: ParticipantId?,
    ) = runTest {
        val deferredParticipantId = CompletableDeferred<ParticipantId>()
        val participants = Random.nextParticipantList(participantId, parentParticipantId)
        val roster = RosterImpl(
            versionId = versionId,
            participantId = participantId,
            parentParticipantId = parentParticipantId,
            step = object : InfinityService.ConferenceStep {

                override fun participant(
                    participantId: ParticipantId,
                ): InfinityService.ParticipantStep = object : InfinityService.ParticipantStep {

                    override fun spotlightOn(token: Token): Call<Boolean> {
                        assertThat(token, "token").isEqualTo(store.token.value)
                        return call {
                            deferredParticipantId.complete(participantId)
                            true
                        }
                    }
                }
            },
        )
        roster.populate(participants)
        roster.spotlight()
        val expected = when (versionId < VersionId.V35_1) {
            true -> participantId
            else -> parentParticipantId ?: participantId
        }
        assertThat(deferredParticipantId.await(), "participantId").isEqualTo(expected)
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

                override fun participant(
                    participantId: ParticipantId,
                ): InfinityService.ParticipantStep {
                    assertThat(participantId, "participantId").isIn(*participantIds.toTypedArray())
                    return object : InfinityService.ParticipantStep {

                        override fun spotlightOff(token: Token): Call<Boolean> {
                            assertThat(token, "token").isEqualTo(store.token.value)
                            return call { throw causes.getValue(participantId) }
                        }
                    }
                }
            },
        )
        roster.populate(participants)
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

                override fun participant(
                    participantId: ParticipantId,
                ): InfinityService.ParticipantStep {
                    assertThat(participantId, "participantId").isIn(*participantIds.toTypedArray())
                    return object : InfinityService.ParticipantStep {

                        override fun spotlightOff(token: Token): Call<Boolean> {
                            assertThat(token, "token").isEqualTo(store.token.value)
                            return call { true }
                        }
                    }
                }
            },
        )
        roster.populate(participants)
        participants.forEach { roster.unspotlight(it.id) }
    }

    private fun `unspotlight(null) returns`(
        versionId: VersionId,
        participantId: ParticipantId,
        parentParticipantId: ParticipantId?,
    ) = runTest {
        val deferredParticipantId = CompletableDeferred<ParticipantId>()
        val participants = Random.nextParticipantList(participantId, parentParticipantId)
        val roster = RosterImpl(
            versionId = versionId,
            participantId = participantId,
            parentParticipantId = parentParticipantId,
            step = object : InfinityService.ConferenceStep {

                override fun participant(
                    participantId: ParticipantId,
                ): InfinityService.ParticipantStep = object : InfinityService.ParticipantStep {

                    override fun spotlightOff(token: Token): Call<Boolean> {
                        assertThat(token, "token").isEqualTo(store.token.value)
                        return call {
                            deferredParticipantId.complete(participantId)
                            true
                        }
                    }
                }
            },
        )
        roster.populate(participants)
        roster.unspotlight()
        val expected = when (versionId < VersionId.V35_1) {
            true -> participantId
            else -> parentParticipantId ?: participantId
        }
        assertThat(deferredParticipantId.await(), "participantId").isEqualTo(expected)
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

                override fun participant(
                    participantId: ParticipantId,
                ): InfinityService.ParticipantStep {
                    assertThat(participantId, "participantId").isIn(*participantIds.toTypedArray())
                    return object : InfinityService.ParticipantStep {

                        override fun clearBuzz(token: Token): Call<Boolean> {
                            assertThat(token, "token").isEqualTo(store.token.value)
                            return call { throw causes.getValue(participantId) }
                        }
                    }
                }
            },
        )
        roster.populate(participants)
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

                override fun participant(
                    participantId: ParticipantId,
                ): InfinityService.ParticipantStep {
                    assertThat(participantId, "participantId").isIn(*participantIds.toTypedArray())
                    return object : InfinityService.ParticipantStep {

                        override fun clearBuzz(token: Token): Call<Boolean> {
                            assertThat(token, "token").isEqualTo(store.token.value)
                            return call { true }
                        }
                    }
                }
            },
        )
        roster.populate(participants)
        participants.forEach { roster.lowerHand(it.id) }
    }

    private fun `lowerHand(null) returns`(
        versionId: VersionId,
        participantId: ParticipantId,
        parentParticipantId: ParticipantId?,
    ) = runTest {
        val deferredParticipantId = CompletableDeferred<ParticipantId>()
        val participants = Random.nextParticipantList(participantId, parentParticipantId)
        val roster = RosterImpl(
            versionId = versionId,
            participantId = participantId,
            parentParticipantId = parentParticipantId,
            step = object : InfinityService.ConferenceStep {

                override fun participant(
                    participantId: ParticipantId,
                ): InfinityService.ParticipantStep = object : InfinityService.ParticipantStep {

                    override fun clearBuzz(token: Token): Call<Boolean> {
                        assertThat(token, "token").isEqualTo(store.token.value)
                        return call {
                            deferredParticipantId.complete(participantId)
                            true
                        }
                    }
                }
            },
        )
        roster.populate(participants)
        roster.lowerHand()
        val expected = when (versionId < VersionId.V35_1) {
            true -> participantId
            else -> parentParticipantId ?: participantId
        }
        assertThat(deferredParticipantId.await(), "participantId").isEqualTo(expected)
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
                    return call { throw cause }
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
                    return call { true }
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
                    return call { throw cause }
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
                    return call { true }
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
                    return call { throw cause }
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
                    return call { true }
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
                    return call { throw cause }
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
                    return call { true }
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
                    return call { throw cause }
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
                    return call { true }
                }
            },
        )
        roster.unmuteAllGuests()
    }

    private fun `allowGuestsToUnmute() throws AllowGuestsToUnmuteException`(
        participantId: ParticipantId,
        parentParticipantId: ParticipantId?,
    ) = runTest {
        val cause = Throwable()
        val roster = RosterImpl(
            participantId = participantId,
            parentParticipantId = parentParticipantId,
            step = object : InfinityService.ConferenceStep {

                override fun setGuestsCanUnmute(
                    request: SetGuestCanUnmuteRequest,
                    token: Token,
                ): Call<Boolean> {
                    assertThat(request::setting).isTrue()
                    assertThat(token, "token").isEqualTo(store.token.value)
                    return call { throw cause }
                }
            },
        )
        assertFailure { roster.allowGuestsToUnmute() }
            .isInstanceOf<AllowGuestsToUnmuteException>()
            .hasCause(cause)
    }

    private fun `allowGuestsToUnmute() returns`(
        participantId: ParticipantId,
        parentParticipantId: ParticipantId?,
    ) = runTest {
        val roster = RosterImpl(
            participantId = participantId,
            parentParticipantId = parentParticipantId,
            step = object : InfinityService.ConferenceStep {

                override fun setGuestsCanUnmute(
                    request: SetGuestCanUnmuteRequest,
                    token: Token,
                ): Call<Boolean> {
                    assertThat(request::setting).isTrue()
                    assertThat(token, "token").isEqualTo(store.token.value)
                    return call { true }
                }
            },
        )
        roster.allowGuestsToUnmute()
    }

    private fun `disallowGuestsToUnmute() throws DisallowGuestsToUnmuteException`(
        participantId: ParticipantId,
        parentParticipantId: ParticipantId?,
    ) = runTest {
        val cause = Throwable()
        val roster = RosterImpl(
            participantId = participantId,
            parentParticipantId = parentParticipantId,
            step = object : InfinityService.ConferenceStep {

                override fun setGuestsCanUnmute(
                    request: SetGuestCanUnmuteRequest,
                    token: Token,
                ): Call<Boolean> {
                    assertThat(request::setting).isFalse()
                    assertThat(token, "token").isEqualTo(store.token.value)
                    return call { throw cause }
                }
            },
        )
        assertFailure { roster.disallowGuestsToUnmute() }
            .isInstanceOf<DisallowGuestsToUnmuteException>()
            .hasCause(cause)
    }

    private fun `disallowGuestsToUnmute() returns`(
        participantId: ParticipantId,
        parentParticipantId: ParticipantId?,
    ) = runTest {
        val roster = RosterImpl(
            participantId = participantId,
            parentParticipantId = parentParticipantId,
            step = object : InfinityService.ConferenceStep {

                override fun setGuestsCanUnmute(
                    request: SetGuestCanUnmuteRequest,
                    token: Token,
                ): Call<Boolean> {
                    assertThat(request::setting).isFalse()
                    assertThat(token, "token").isEqualTo(store.token.value)
                    return call { true }
                }
            },
        )
        roster.disallowGuestsToUnmute()
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
                    return call { throw cause }
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
                    return call { true }
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
            clientAudioMuted = nextBoolean(),
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

    private suspend fun Roster.populate(participants: List<Participant>) = this.participants.test {
        event.subscriptionCount.first { it > 0 }
        assertThat(awaitItem(), "participants").isEmpty()
        event.emit(ParticipantSyncBeginEvent)
        event.emit(ParticipantSyncEndEvent)
        participants.forEachIndexed { index, participant ->
            val response = participant.toParticipantResponse()
            val e = ParticipantCreateEvent(response)
            event.emit(e)
            assertThat(awaitItem(), "participants")
                .index(index)
                .isEqualTo(participant)
        }
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
        clientAudioMuted = clientAudioMuted,
    )

    @Suppress("TestFunctionName")
    private fun TestScope.RosterImpl(
        participantId: ParticipantId,
        parentParticipantId: ParticipantId?,
        step: InfinityService.ConferenceStep = object : InfinityService.ConferenceStep {
            override fun participant(
                participantId: ParticipantId,
            ): InfinityService.ParticipantStep = object : InfinityService.ParticipantStep {}
        },
        versionId: VersionId = VersionId.V35,
        serviceType: ServiceType = ServiceType.CONFERENCE,
    ) = RosterImpl(
        scope = backgroundScope,
        event = event,
        versionId = versionId,
        serviceType = serviceType,
        participantId = participantId,
        parentParticipantId = parentParticipantId,
        store = store,
        step = step,
    )
}
