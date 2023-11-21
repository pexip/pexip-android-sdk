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

import com.pexip.sdk.api.Event
import com.pexip.sdk.api.infinity.ParticipantCreateEvent
import com.pexip.sdk.api.infinity.ParticipantDeleteEvent
import com.pexip.sdk.api.infinity.ParticipantResponse
import com.pexip.sdk.api.infinity.ParticipantSyncBeginEvent
import com.pexip.sdk.api.infinity.ParticipantSyncEndEvent
import com.pexip.sdk.api.infinity.ParticipantUpdateEvent
import com.pexip.sdk.conference.Participant
import com.pexip.sdk.conference.Role
import com.pexip.sdk.conference.Roster
import com.pexip.sdk.conference.ServiceType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.stateIn
import java.util.UUID
import com.pexip.sdk.api.infinity.Role as ApiRole
import com.pexip.sdk.api.infinity.ServiceType as ApiServiceType

internal class RosterImpl(scope: CoroutineScope, event: Flow<Event>) : Roster {

    private val _participants = channelFlow {
        var syncing = false
        val participants = mutableMapOf<UUID, Participant>()

        suspend fun maybeSendParticipants() {
            if (syncing) return
            send(participants.values.toList())
        }

        event.collect {
            when (it) {
                is ParticipantSyncBeginEvent -> {
                    syncing = true
                    participants.clear()
                }
                is ParticipantSyncEndEvent -> {
                    syncing = false
                    maybeSendParticipants()
                }
                is ParticipantCreateEvent -> {
                    participants.put(it.response)
                    maybeSendParticipants()
                }
                is ParticipantUpdateEvent -> {
                    participants.put(it.response)
                    maybeSendParticipants()
                }
                is ParticipantDeleteEvent -> {
                    participants -= it.id
                    maybeSendParticipants()
                }
                else -> Unit
            }
        }
    }

    override val participants: StateFlow<List<Participant>> = _participants.stateIn(
        scope = scope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList(),
    )

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
