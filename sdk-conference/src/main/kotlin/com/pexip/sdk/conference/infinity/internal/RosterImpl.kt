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

import com.pexip.sdk.api.Call
import com.pexip.sdk.api.Event
import com.pexip.sdk.api.coroutines.await
import com.pexip.sdk.api.infinity.ConferenceUpdateEvent
import com.pexip.sdk.api.infinity.InfinityService
import com.pexip.sdk.api.infinity.ParticipantCreateEvent
import com.pexip.sdk.api.infinity.ParticipantDeleteEvent
import com.pexip.sdk.api.infinity.ParticipantResponse
import com.pexip.sdk.api.infinity.ParticipantSyncBeginEvent
import com.pexip.sdk.api.infinity.ParticipantSyncEndEvent
import com.pexip.sdk.api.infinity.ParticipantUpdateEvent
import com.pexip.sdk.api.infinity.PresentationStartEvent
import com.pexip.sdk.api.infinity.PresentationStopEvent
import com.pexip.sdk.api.infinity.RoleRequest
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
import com.pexip.sdk.conference.Role
import com.pexip.sdk.conference.Roster
import com.pexip.sdk.conference.ServiceType
import com.pexip.sdk.conference.SpotlightException
import com.pexip.sdk.conference.UnlockException
import com.pexip.sdk.conference.UnmuteAllGuestsException
import com.pexip.sdk.conference.UnmuteException
import com.pexip.sdk.conference.UnmuteVideoException
import com.pexip.sdk.conference.UnspotlightException
import com.pexip.sdk.core.retry
import com.pexip.sdk.infinity.ParticipantId
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import com.pexip.sdk.api.infinity.Role as ApiRole
import com.pexip.sdk.api.infinity.ServiceType as ApiServiceType

internal class RosterImpl(
    scope: CoroutineScope,
    event: Flow<Event>,
    private val participantId: ParticipantId,
    private val store: TokenStore,
    private val step: InfinityService.ConferenceStep,
) : Roster {

    private val mutex = Mutex()
    private val participantMap = mutableMapOf<ParticipantId, Participant>()
    private val participantStepMap = mutableMapOf<ParticipantId, InfinityService.ParticipantStep>()

    private val participantMapFlow = channelFlow {
        var syncing = false

        suspend fun maybeSendParticipants() {
            if (syncing) return
            send(participantMap.toMap())
        }

        event.collect {
            when (it) {
                is ParticipantSyncBeginEvent -> mutex.withLock {
                    syncing = true
                    participantMap.clear()
                    participantStepMap.clear()
                }
                is ParticipantSyncEndEvent -> mutex.withLock {
                    syncing = false
                    maybeSendParticipants()
                }
                is ParticipantCreateEvent -> mutex.withLock {
                    participantMap.put(it.response)
                    maybeSendParticipants()
                }
                is ParticipantUpdateEvent -> mutex.withLock {
                    participantMap.put(it.response)
                    maybeSendParticipants()
                }
                is ParticipantDeleteEvent -> mutex.withLock {
                    participantMap -= it.id
                    participantStepMap -= it.id
                    maybeSendParticipants()
                }
                is StageEvent -> mutex.withLock {
                    for (speaker in it.speakers) {
                        val participant = participantMap[speaker.participantId] ?: continue
                        if (speaker.speaking == participant.speaking) continue
                        participantMap[participant.id] =
                            participant.copy(speaking = speaker.speaking)
                    }
                    maybeSendParticipants()
                }
                else -> Unit
            }
        }
    }.stateIn(scope, SharingStarted.Eagerly, emptyMap())

    override val participants: StateFlow<List<Participant>> =
        participantMapFlow.map { it.values.toList() }
            .stateIn(scope, SharingStarted.Eagerly, emptyList())

    override val me: StateFlow<Participant?> =
        participantMapFlow.map { it[participantId] }.stateIn(scope, SharingStarted.Eagerly, null)

    override val presenter: StateFlow<Participant?> = combine(
        flow = event.filter { it is PresentationStartEvent || it is PresentationStopEvent },
        flow2 = participantMapFlow,
        transform = { event, map ->
            when (event) {
                is PresentationStartEvent -> map[event.presenterId]
                else -> null
            }
        },
    ).stateIn(scope, SharingStarted.Eagerly, null)

    override val locked: StateFlow<Boolean> =
        event.filterIsInstance<ConferenceUpdateEvent>().map { it.locked }
            .stateIn(scope, SharingStarted.Eagerly, false)

    override val allGuestsMuted: StateFlow<Boolean> =
        event.filterIsInstance<ConferenceUpdateEvent>().map { it.guestsMuted }
            .stateIn(scope, SharingStarted.Eagerly, false)

    override suspend fun admit(participantId: ParticipantId) {
        perform(::AdmitException) {
            val step = participantStep(participantId) ?: return
            step.unlock(it)
        }
    }

    override suspend fun disconnect(participantId: ParticipantId?) {
        perform(::DisconnectException) {
            val step = participantStep(participantId) ?: return
            step.disconnect(it)
        }
    }

    override suspend fun makeHost(participantId: ParticipantId?) {
        perform(::MakeHostException) {
            val step = participantStep(participantId) ?: return
            step.role(RoleRequest(ApiRole.HOST), it)
        }
    }

    override suspend fun makeGuest(participantId: ParticipantId?) {
        perform(::MakeGuestException) {
            val step = participantStep(participantId) ?: return
            step.role(RoleRequest(ApiRole.GUEST), it)
        }
    }

    override suspend fun mute(participantId: ParticipantId?) {
        perform(::MuteException) {
            val step = participantStep(participantId) ?: return
            step.mute(it)
        }
    }

    override suspend fun unmute(participantId: ParticipantId?) {
        perform(::UnmuteException) {
            val step = participantStep(participantId) ?: return
            step.unmute(it)
        }
    }

    override suspend fun muteVideo(participantId: ParticipantId?) {
        perform(::MuteVideoException) {
            val step = participantStep(participantId) ?: return
            step.videoMuted(it)
        }
    }

    override suspend fun unmuteVideo(participantId: ParticipantId?) {
        perform(::UnmuteVideoException) {
            val step = participantStep(participantId) ?: return
            step.videoUnmuted(it)
        }
    }

    override suspend fun spotlight(participantId: ParticipantId?) {
        perform(::SpotlightException) {
            val step = participantStep(participantId) ?: return
            step.spotlightOn(it)
        }
    }

    override suspend fun unspotlight(participantId: ParticipantId?) {
        perform(::UnspotlightException) {
            val step = participantStep(participantId) ?: return
            step.spotlightOff(it)
        }
    }

    override suspend fun raiseHand(participantId: ParticipantId?) {
        perform(::RaiseHandException) {
            val step = participantStep(participantId) ?: return
            step.buzz(it)
        }
    }

    override suspend fun lowerHand(participantId: ParticipantId?) {
        perform(::LowerHandException) {
            val step = participantStep(participantId) ?: return
            step.clearBuzz(it)
        }
    }

    override suspend fun lowerAllHands() {
        perform(::LowerAllHandsException, step::clearAllBuzz)
    }

    override suspend fun lock() {
        perform(::LockException, step::lock)
    }

    override suspend fun unlock() {
        perform(::UnlockException, step::unlock)
    }

    override suspend fun muteAllGuests() {
        perform(::MuteAllGuestsException, step::muteGuests)
    }

    override suspend fun unmuteAllGuests() {
        perform(::UnmuteAllGuestsException, step::unmuteGuests)
    }

    override suspend fun disconnectAll() {
        perform(::DisconnectAllException, step::disconnect)
    }

    private suspend inline fun <T> perform(
        error: (Throwable) -> Throwable,
        call: (Token) -> Call<T>,
    ): T = try {
        retry { call(store.get()).await() }
    } catch (e: CancellationException) {
        throw e
    } catch (t: Throwable) {
        throw error(t)
    }

    private suspend fun participantStep(participantId: ParticipantId?) = mutex.withLock {
        when (val id = participantId ?: this.participantId) {
            in participantMap -> participantStepMap.getOrPut(id) { step.participant(id) }
            else -> null
        }
    }

    private fun MutableMap<ParticipantId, Participant>.put(response: ParticipantResponse) = put(
        key = response.id,
        value = Participant(
            id = response.id,
            startTime = response.startTime,
            buzzTime = response.buzzTime,
            spotlightTime = response.spotlightTime,
            displayName = response.displayName,
            overlayText = response.overlayText,
            me = response.id == participantId,
            speaking = get(response.id)?.speaking ?: false,
            audioMuted = response.audioMuted,
            videoMuted = response.videoMuted,
            presenting = response.presenting,
            muteSupported = response.muteSupported,
            transferSupported = response.transferSupported,
            disconnectSupported = response.disconnectSupported,
            role = when (response.role) {
                ApiRole.HOST -> Role.HOST
                ApiRole.GUEST -> Role.GUEST
                ApiRole.UNKNOWN -> Role.UNKNOWN
            },
            serviceType = when (response.serviceType) {
                ApiServiceType.CONNECTING -> ServiceType.CONNECTING
                ApiServiceType.WAITING_ROOM -> ServiceType.WAITING_ROOM
                ApiServiceType.IVR -> ServiceType.IVR
                ApiServiceType.CONFERENCE -> ServiceType.CONFERENCE
                ApiServiceType.LECTURE -> ServiceType.LECTURE
                ApiServiceType.GATEWAY -> ServiceType.GATEWAY
                ApiServiceType.TEST_CALL -> ServiceType.TEST_CALL
                ApiServiceType.UNKNOWN -> ServiceType.UNKNOWN
            },
        ),
    )
}
