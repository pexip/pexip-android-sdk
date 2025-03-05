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

import com.pexip.sdk.api.Call
import com.pexip.sdk.api.Event
import com.pexip.sdk.api.infinity.ConferenceUpdateEvent
import com.pexip.sdk.api.infinity.InfinityService.ConferenceStep
import com.pexip.sdk.api.infinity.InfinityService.ParticipantStep
import com.pexip.sdk.api.infinity.ParticipantCreateEvent
import com.pexip.sdk.api.infinity.ParticipantDeleteEvent
import com.pexip.sdk.api.infinity.ParticipantResponse
import com.pexip.sdk.api.infinity.ParticipantSyncBeginEvent
import com.pexip.sdk.api.infinity.ParticipantSyncEndEvent
import com.pexip.sdk.api.infinity.ParticipantUpdateEvent
import com.pexip.sdk.api.infinity.PresentationStartEvent
import com.pexip.sdk.api.infinity.PresentationStopEvent
import com.pexip.sdk.api.infinity.RoleRequest
import com.pexip.sdk.api.infinity.SetGuestCanUnmuteRequest
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
import com.pexip.sdk.core.retry
import com.pexip.sdk.infinity.ParticipantId
import com.pexip.sdk.infinity.Role
import com.pexip.sdk.infinity.ServiceType
import com.pexip.sdk.infinity.VersionId
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
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

internal class RosterImpl(
    scope: CoroutineScope,
    event: Flow<Event>,
    private val versionId: VersionId,
    private val serviceType: ServiceType,
    private val participantId: ParticipantId,
    private val parentParticipantId: ParticipantId?,
    private val store: TokenStore,
    private val step: ConferenceStep,
) : Roster {

    private val mutex = Mutex()
    private val participantMap = mutableMapOf<ParticipantId, Participant>()
    private val participantStepMap = mutableMapOf<ParticipantId, ParticipantStep>()

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
                    // Since we know our IDs, we can add ParticipantStep instances here instead of
                    // waiting for `participant_create`; additionally, in presence of a parent, we
                    // will never see a `participant_create` with a child participant ID.
                    participantStepMap[participantId] = step.participant(participantId)
                    parentParticipantId?.let { id -> participantStepMap[id] = step.participant(id) }
                }
                is ParticipantSyncEndEvent -> mutex.withLock {
                    syncing = false
                    maybeSendParticipants()
                }
                is ParticipantCreateEvent -> mutex.withLock {
                    participantMap.put(it.response)
                    participantStepMap.getOrPut(it.response.id) { step.participant(it.response.id) }
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

    /**
     * On versions prior to 35.1 we would like to use our participant ID even if we have a parent
     * since it's not possible to modify the parent directly unless we are a host;
     *
     * Starting with 35.1 child participant can modify their parent regardless of their role and
     * thus parent participant ID can take precedence.
     *
     * Note that this does not apply to certain methods like `mute` and `unmute` prior to 35.1.
     * See mcu/38673 for details.
     */
    private val selfParticipantId: ParticipantId
        get() = when (versionId < VersionId.V35_1) {
            true -> participantId
            else -> parentParticipantId ?: participantId
        }

    override val participants: StateFlow<List<Participant>> = participantMapFlow
        .map { it.values.toList() }
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    override val me: StateFlow<Participant?> = participantMapFlow
        .map { it[parentParticipantId ?: participantId] }
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

    override val locked: StateFlow<Boolean> = event
        .filterIsInstance<ConferenceUpdateEvent>()
        .map { it.locked }
        .stateIn(scope, SharingStarted.Eagerly, false)

    override val allGuestsMuted: StateFlow<Boolean> = event
        .filterIsInstance<ConferenceUpdateEvent>()
        .map { it.guestsMuted }
        .stateIn(scope, SharingStarted.Eagerly, false)

    override val guestsCanUnmute: StateFlow<Boolean?> = event
        .filterIsInstance<ConferenceUpdateEvent>()
        .map { it.guestsCanUnmute }
        .stateIn(scope, SharingStarted.Eagerly, null)

    override suspend fun admit(participantId: ParticipantId) {
        perform(participantId, ParticipantStep::unlock, ::AdmitException)
    }

    override suspend fun disconnect(participantId: ParticipantId?) {
        perform(
            participantId = participantId ?: selfParticipantId,
            callFactory = ParticipantStep::disconnect,
            errorFactory = ::DisconnectException,
        )
    }

    override suspend fun makeHost(participantId: ParticipantId?) {
        perform(
            participantId = participantId ?: selfParticipantId,
            callFactory = { role(RoleRequest(Role.HOST), it) },
            errorFactory = ::MakeHostException,
        )
    }

    override suspend fun makeGuest(participantId: ParticipantId?) {
        perform(
            participantId = participantId ?: selfParticipantId,
            callFactory = { role(RoleRequest(Role.GUEST), it) },
            errorFactory = ::MakeGuestException,
        )
    }

    override suspend fun clientMute() {
        perform(
            // `client_mute` can only be performed on self and we should prefer parent participant
            // due to `mute` fallback on versions earlier than 36
            participantId = parentParticipantId ?: participantId,
            callFactory = when {
                serviceType == ServiceType.GATEWAY -> ParticipantStep::mute
                versionId < VersionId.V36 -> ParticipantStep::mute
                else -> ParticipantStep::clientMute
            },
            errorFactory = ::ClientMuteException,
        )
    }

    override suspend fun clientUnmute(): Unit = coroutineScope {
        // `client_unmute` can only be performed on self and we should prefer parent participant
        // due to `unmute` fallback on versions earlier than 36
        val participantId = parentParticipantId ?: participantId
        val unmute = async {
            perform(
                participantId = participantId,
                callFactory = ParticipantStep::unmute,
                errorFactory = ::ClientUnmuteException,
            )
        }
        val clientUnmute = when {
            serviceType == ServiceType.GATEWAY -> null
            versionId < VersionId.V36 -> null
            else -> async {
                perform(
                    participantId = participantId,
                    callFactory = ParticipantStep::clientUnmute,
                    errorFactory = ::ClientUnmuteException,
                )
            }
        }
        unmute.await()
        clientUnmute?.await()
    }

    override suspend fun mute(participantId: ParticipantId?) {
        perform(
            // Always try to use `mute` as a parent participant (if we're a child);
            // Note that this will fail if the parent is a guest.
            participantId = participantId ?: this.parentParticipantId ?: this.participantId,
            callFactory = ParticipantStep::mute,
            errorFactory = ::MuteException,
        )
    }

    override suspend fun unmute(participantId: ParticipantId?) {
        perform(
            // Always try to use `unmute` as a parent participant (if we're a child);
            // Note that this will fail if the parent is a guest.
            participantId = participantId ?: this.parentParticipantId ?: this.participantId,
            callFactory = ParticipantStep::unmute,
            errorFactory = ::UnmuteException,
        )
    }

    override suspend fun muteVideo(participantId: ParticipantId?) {
        perform(
            participantId = participantId ?: selfParticipantId,
            callFactory = ParticipantStep::videoMuted,
            errorFactory = ::MuteVideoException,
        )
    }

    override suspend fun unmuteVideo(participantId: ParticipantId?) {
        perform(
            participantId = participantId ?: selfParticipantId,
            callFactory = ParticipantStep::videoUnmuted,
            errorFactory = ::UnmuteVideoException,
        )
    }

    override suspend fun spotlight(participantId: ParticipantId?) {
        perform(
            participantId = participantId ?: selfParticipantId,
            callFactory = ParticipantStep::spotlightOn,
            errorFactory = ::SpotlightException,
        )
    }

    override suspend fun unspotlight(participantId: ParticipantId?) {
        perform(
            participantId = participantId ?: selfParticipantId,
            callFactory = ParticipantStep::spotlightOff,
            errorFactory = ::UnspotlightException,
        )
    }

    override suspend fun raiseHand(participantId: ParticipantId?) {
        perform(
            participantId = participantId ?: selfParticipantId,
            callFactory = ParticipantStep::buzz,
            errorFactory = ::RaiseHandException,
        )
    }

    override suspend fun lowerHand(participantId: ParticipantId?) {
        perform(
            participantId = participantId ?: selfParticipantId,
            callFactory = ParticipantStep::clearBuzz,
            errorFactory = ::LowerHandException,
        )
    }

    override suspend fun lowerAllHands() {
        perform(ConferenceStep::clearAllBuzz, ::LowerAllHandsException)
    }

    override suspend fun lock() {
        perform(ConferenceStep::lock, ::LockException)
    }

    override suspend fun unlock() {
        perform(ConferenceStep::unlock, ::UnlockException)
    }

    override suspend fun muteAllGuests() {
        perform(ConferenceStep::muteGuests, ::MuteAllGuestsException)
    }

    override suspend fun unmuteAllGuests() {
        perform(ConferenceStep::unmuteGuests, ::UnmuteAllGuestsException)
    }

    override suspend fun allowGuestsToUnmute() {
        perform(
            callFactory = { setGuestsCanUnmute(SetGuestCanUnmuteRequest(true), it) },
            errorFactory = ::AllowGuestsToUnmuteException,
        )
    }

    override suspend fun disallowGuestsToUnmute() {
        perform(
            callFactory = { setGuestsCanUnmute(SetGuestCanUnmuteRequest(false), it) },
            errorFactory = ::DisallowGuestsToUnmuteException,
        )
    }

    override suspend fun disconnectAll() {
        perform(ConferenceStep::disconnect, ::DisconnectAllException)
    }

    private suspend inline fun <T> perform(
        participantId: ParticipantId,
        callFactory: ParticipantStep.(Token) -> Call<T>,
        errorFactory: (Throwable) -> Throwable,
    ) = try {
        retry {
            mutex.withLock { participantStepMap[participantId] }
                ?.callFactory(store.token.value)
                ?.await()
        }
    } catch (e: CancellationException) {
        throw e
    } catch (t: Throwable) {
        throw errorFactory(t)
    }

    private suspend inline fun <T> perform(
        callFactory: ConferenceStep.(Token) -> Call<T>,
        errorFactory: (Throwable) -> Throwable,
    ) = try {
        retry { step.callFactory(store.token.value).await() }
    } catch (e: CancellationException) {
        throw e
    } catch (t: Throwable) {
        throw errorFactory(t)
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
            me = response.id == (parentParticipantId ?: participantId),
            speaking = get(response.id)?.speaking ?: false,
            audioMuted = response.audioMuted,
            videoMuted = response.videoMuted,
            presenting = response.presenting,
            muteSupported = response.muteSupported,
            transferSupported = response.transferSupported,
            disconnectSupported = response.disconnectSupported,
            role = response.role,
            serviceType = response.serviceType,
            callTag = response.callTag,
            clientAudioMuted = response.clientAudioMuted,
        ),
    )
}
