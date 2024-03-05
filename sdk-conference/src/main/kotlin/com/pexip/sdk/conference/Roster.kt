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

import kotlinx.coroutines.flow.StateFlow
import java.util.UUID

/**
 * Handles conference participants.
 */
public interface Roster {

    /**
     * A [StateFlow] that represents participants of this conference.
     */
    public val participants: StateFlow<List<Participant>>

    /**
     * A [StateFlow] that represents *you* as the participant of this conference.
     */
    public val me: StateFlow<Participant?>

    /**
     * Raises hand of the specified participant or self.
     *
     * @param participantId an ID of the participant, null for self
     * @throws RaiseHandException if the operation failed
     */
    public suspend fun raiseHand(participantId: UUID? = null)

    /**
     * Lowers hand of the specified participant or self.
     *
     * @param participantId an ID of the participant, null for self
     * @throws LowerHandException if the operation failed
     */
    public suspend fun lowerHand(participantId: UUID? = null)

    /**
     * Lowers all hands.
     *
     * @throws LowerAllHandsException if the operation failed
     */
    public suspend fun lowerAllHands()
}
