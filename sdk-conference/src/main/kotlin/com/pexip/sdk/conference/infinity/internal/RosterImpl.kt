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

import com.pexip.sdk.api.Event
import com.pexip.sdk.api.coroutines.await
import com.pexip.sdk.api.infinity.InfinityService
import com.pexip.sdk.api.infinity.ParticipantCreateEvent
import com.pexip.sdk.api.infinity.ParticipantDeleteEvent
import com.pexip.sdk.api.infinity.ParticipantResponse
import com.pexip.sdk.api.infinity.ParticipantSyncBeginEvent
import com.pexip.sdk.api.infinity.ParticipantSyncEndEvent
import com.pexip.sdk.api.infinity.ParticipantUpdateEvent
import com.pexip.sdk.api.infinity.PresentationStartEvent
import com.pexip.sdk.api.infinity.PresentationStopEvent
import com.pexip.sdk.api.infinity.TokenStore
import com.pexip.sdk.conference.DisconnectException
import com.pexip.sdk.conference.LowerAllHandsException
import com.pexip.sdk.conference.LowerHandException
import com.pexip.sdk.conference.Participant
import com.pexip.sdk.conference.RaiseHandException
import com.pexip.sdk.conference.Role
import com.pexip.sdk.conference.Roster
import com.pexip.sdk.conference.ServiceType
import com.pexip.sdk.core.retry
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID
import com.pexip.sdk.api.infinity.Role as ApiRole
import com.pexip.sdk.api.infinity.ServiceType as ApiServiceType

internal class RosterImpl(
    scope: CoroutineScope,
    event: Flow<Event>,
    private val participantId: UUID,
    private val store: TokenStore,
    private val step: InfinityService.ConferenceStep,
) : Roster {

    private val mutex = Mutex()
    private val participantMap = mutableMapOf<UUID, Participant>()
    private val participantStepMap = mutableMapOf<UUID, InfinityService.ParticipantStep>()

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
                else -> Unit
            }
        }
    }.stateIn(scope, SharingStarted.Eagerly, emptyMap())

    override val participants: StateFlow<List<Participant>> = participantMapFlow
        .map { it.values.toList() }
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    override val me: StateFlow<Participant?> = participantMapFlow
        .map { it[participantId] }
        .stateIn(scope, SharingStarted.Eagerly, null)

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

    override suspend fun disconnect(participantId: UUID?) {
        try {
            val step = participantStep(participantId) ?: return
            retry { step.disconnect(store.get()).await() }
        } catch (e: CancellationException) {
            throw e
        } catch (t: Throwable) {
            throw DisconnectException(t)
        }
    }

    override suspend fun raiseHand(participantId: UUID?) {
        try {
            val step = participantStep(participantId) ?: return
            retry { step.buzz(store.get()).await() }
        } catch (e: CancellationException) {
            throw e
        } catch (t: Throwable) {
            throw RaiseHandException(t)
        }
    }

    override suspend fun lowerHand(participantId: UUID?) {
        try {
            val step = participantStep(participantId) ?: return
            retry { step.clearBuzz(store.get()).await() }
        } catch (e: CancellationException) {
            throw e
        } catch (t: Throwable) {
            throw LowerHandException(t)
        }
    }

    override suspend fun lowerAllHands() {
        try {
            retry { step.clearAllBuzz(store.get()).await() }
        } catch (e: CancellationException) {
            throw e
        } catch (t: Throwable) {
            throw LowerAllHandsException(t)
        }
    }

    private suspend fun participantStep(participantId: UUID?) = mutex.withLock {
        when (val id = participantId ?: this.participantId) {
            in participantMap -> participantStepMap.getOrPut(id) { step.participant(id) }
            else -> null
        }
    }

    private fun MutableMap<UUID, Participant>.put(response: ParticipantResponse) = put(
        key = response.id,
        value = Participant(
            id = response.id,
            startTime = response.startTime,
            buzzTime = response.buzzTime,
            spotlightTime = response.spotlightTime,
            displayName = response.displayName,
            overlayText = response.overlayText,
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
