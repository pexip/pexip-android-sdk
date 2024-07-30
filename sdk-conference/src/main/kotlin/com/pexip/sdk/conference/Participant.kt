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
import com.pexip.sdk.infinity.Role
import com.pexip.sdk.infinity.ServiceType
import kotlinx.datetime.Instant

/**
 * A conference participant.
 *
 * @property id an unique ID of this participant
 * @property role a role of this participant
 * @property serviceType a service type of this participant
 * @property startTime a UNIX timestamp representing the moment in time when this participant
 * joined the conference, or null
 * @property buzzTime a UNIX timestamp representing the moment in time when this participant
 * raised their hand, or null if their hand is not currently raised
 * @property spotlightTime a UNIX timestamp representing the moment in time when this participant
 * was spotlit, or null if they're currently not spotlit
 * @property displayName a display name of this participant
 * @property overlayText an overlay text that is shown on top of this participant's remote video
 * @property me whether this participant is *you*
 * @property audioMuted whether this participant has their audio muted
 * @property videoMuted whether this participant has their video muted
 * @property presenting whether this participant is currently presenting
 * @property muteSupported whether this participant can be muted
 * @property transferSupported whether this participant can be transferred to a different conference
 * @property disconnectSupported whether this participant can be disconnected
 * @property callTag a call tag set on this participant
 */
public data class Participant(
    val id: ParticipantId,
    val role: Role,
    val serviceType: ServiceType,
    val startTime: Instant?,
    val buzzTime: Instant?,
    val spotlightTime: Instant?,
    val displayName: String,
    val overlayText: String,
    val me: Boolean = false,
    val speaking: Boolean = false,
    val audioMuted: Boolean = false,
    val videoMuted: Boolean = false,
    val presenting: Boolean = false,
    val muteSupported: Boolean = false,
    val transferSupported: Boolean = false,
    val disconnectSupported: Boolean = false,
    val callTag: String = "",
)
