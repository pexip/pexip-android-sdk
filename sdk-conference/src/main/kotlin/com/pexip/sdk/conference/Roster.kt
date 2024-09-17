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
package com.pexip.sdk.conference

import com.pexip.sdk.infinity.ParticipantId
import kotlinx.coroutines.flow.StateFlow

/**
 * Handles conference participants.
 */
public interface Roster {

    /**
     * A [StateFlow] that represents participants of this conference.
     */
    public val participants: StateFlow<List<Participant>>
        get() = throw NotImplementedError()

    /**
     * A [StateFlow] that represents *you* as the participant of this conference.
     */
    public val me: StateFlow<Participant?>
        get() = throw NotImplementedError()

    /**
     * A [StateFlow] that represents the participant that is currently sharing a presentation.
     *
     * Note that the value will always be `null` when you're sharing the presentation.
     */
    public val presenter: StateFlow<Participant?>
        get() = throw NotImplementedError()

    /**
     * A [StateFlow] that represents whether the conference is locked.
     *
     * When a conference is locked, participants waiting to join are held at
     * a "Waiting for Host" screen. These settings are only available to conference hosts.
     */
    public val locked: StateFlow<Boolean>
        get() = throw NotImplementedError()

    /**
     * A [StateFlow] that represents whether guests in the conference all muted.
     *
     * When muted, no guest participants can speak unless they are explicitly unmuted.
     * This setting is only available to conference hosts.
     */
    public val allGuestsMuted: StateFlow<Boolean>
        get() = throw NotImplementedError()

    /**
     * A [StateFlow] that represents whether guests in the conference can unmute themselves, or null.
     *
     * Null signals whether that particular instance of Infinity supports modifying this property
     * and can be used to hide/show the appropriate UI controls without additional version checks.
     *
     * When muted, no guest participants can speak unless they are explicitly unmuted.
     * This setting is only available to conference hosts.
     */
    public val guestsCanUnmute: StateFlow<Boolean?>
        get() = throw NotImplementedError()

    /**
     * Lets a specified participant into the conference from the waiting room of a locked conference.
     *
     * @param participantId an ID of the participant
     * @throws AdmitException if the operation failed
     */
    public suspend fun admit(participantId: ParticipantId): Unit = throw NotImplementedError()

    /**
     * Disconnects the specified participant or self.
     *
     * @param participantId an ID of the participant, null for self
     * @throws DisconnectException if the operation failed
     */
    public suspend fun disconnect(participantId: ParticipantId? = null): Unit =
        throw NotImplementedError()

    /**
     * Changes the role of the participant to host.
     *
     * @param participantId an ID of the participant, null for self
     * @throws MakeHostException if the operation failed
     */
    public suspend fun makeHost(participantId: ParticipantId? = null): Unit =
        throw NotImplementedError()

    /**
     * Changes the role of the participant to guest.
     *
     * @param participantId an ID of the participant, null for self
     * @throws MakeGuestException if the operation failed
     */
    public suspend fun makeGuest(participantId: ParticipantId? = null): Unit =
        throw NotImplementedError()

    /**
     * Signals that the local microphone has been muted.
     *
     * Can only be called on self.
     *
     * @throws ClientMuteException if the operation failed
     */
    public suspend fun clientMute(): Unit = throw NotImplementedError()

    /**
     * Signals that the local microphone has been unmuted.
     *
     * Can only be called on self.
     *
     * @throws ClientUnmuteException if the operation failed
     */
    public suspend fun clientUnmute(): Unit = throw NotImplementedError()

    /**
     * Mutes the specified participant or self.
     *
     * @param participantId an ID of the participant, null for self
     * @throws MuteException if the operation failed
     */
    public suspend fun mute(participantId: ParticipantId? = null): Unit =
        throw NotImplementedError()

    /**
     * Unmutes the specified participant or self.
     *
     * @param participantId an ID of the participant, null for self
     * @throws UnmuteException if the operation failed
     */
    public suspend fun unmute(participantId: ParticipantId? = null): Unit =
        throw NotImplementedError()

    /**
     * Mutes the video of the specified participant or self.
     *
     * @param participantId an ID of the participant, null for self
     * @throws MuteVideoException if the operation failed
     */
    public suspend fun muteVideo(participantId: ParticipantId? = null): Unit =
        throw NotImplementedError()

    /**
     * Unmutes the video of the specified participant or self.
     *
     * @param participantId an ID of the participant, null for self
     * @throws UnmuteVideoException if the operation failed
     */
    public suspend fun unmuteVideo(participantId: ParticipantId? = null): Unit =
        throw NotImplementedError()

    /**
     * Enables the "spotlight" on a participant or self.
     *
     * @param participantId an ID of the participant, null for self
     * @throws SpotlightException if the operation failed
     */
    public suspend fun spotlight(participantId: ParticipantId? = null): Unit =
        throw NotImplementedError()

    /**
     * Disables the "spotlight" on a participant or self.
     *
     * @param participantId an ID of the participant, null for self
     * @throws UnspotlightException if the operation failed
     */
    public suspend fun unspotlight(participantId: ParticipantId? = null): Unit =
        throw NotImplementedError()

    /**
     * Raises hand of the specified participant or self.
     *
     * @param participantId an ID of the participant, null for self
     * @throws RaiseHandException if the operation failed
     */
    public suspend fun raiseHand(participantId: ParticipantId? = null): Unit =
        throw NotImplementedError()

    /**
     * Lowers hand of the specified participant or self.
     *
     * @param participantId an ID of the participant, null for self
     * @throws LowerHandException if the operation failed
     */
    public suspend fun lowerHand(participantId: ParticipantId? = null): Unit =
        throw NotImplementedError()

    /**
     * Lowers all hands.
     *
     * @throws LowerAllHandsException if the operation failed
     */
    public suspend fun lowerAllHands(): Unit = throw NotImplementedError()

    /**
     * Locks the conference.
     *
     * @throws LockException if the operation failed
     */
    public suspend fun lock(): Unit = throw NotImplementedError()

    /**
     * Unlocks the conference.
     *
     * @throws UnlockException if the operation failed
     */
    public suspend fun unlock(): Unit = throw NotImplementedError()

    /**
     * Mutes all guests in a conference.
     *
     * @throws MuteAllGuestsException if the operation failed
     */
    public suspend fun muteAllGuests(): Unit = throw NotImplementedError()

    /**
     * Unmutes all guests in a conference.
     *
     * @throws UnmuteAllGuestsException if the operation failed
     */
    public suspend fun unmuteAllGuests(): Unit = throw NotImplementedError()

    /**
     * Allows guests to unmute themselves.
     *
     * @throws AllowGuestsToUnmuteException if the operation failed
     */
    public suspend fun allowGuestsToUnmute(): Unit = throw NotImplementedError()

    /**
     * Disallows guests to unmute themselves.
     *
     * @throws DisallowGuestsToUnmuteException if the operation failed
     */
    public suspend fun disallowGuestsToUnmute(): Unit = throw NotImplementedError()

    /**
     * Disconnects all conference participants.
     *
     * @throws DisconnectAllException if the operation failed
     */
    public suspend fun disconnectAll(): Unit = throw NotImplementedError()
}
