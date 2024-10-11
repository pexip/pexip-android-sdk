/*
 * Copyright 2022-2024 Pexip AS
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
import kotlinx.datetime.Instant

public sealed interface ConferenceEvent {

    public val at: Instant
}

@Deprecated(
    message = "Use Roster.presenter instead to observe presentation state.",
    level = DeprecationLevel.ERROR,
)
public data class PresentationStartConferenceEvent(
    override val at: Instant,
    val presenterId: ParticipantId,
    val presenterName: String,
) : ConferenceEvent

@Deprecated(
    message = "Use Roster.presenter instead to observe presentation state.",
    level = DeprecationLevel.ERROR,
)
public data class PresentationStopConferenceEvent(override val at: Instant) : ConferenceEvent

public data class ReferConferenceEvent(
    override val at: Instant,
    val conferenceAlias: String,
    val token: String,
) : ConferenceEvent

public data class DisconnectConferenceEvent(
    override val at: Instant,
    val reason: String,
) : ConferenceEvent

public data class FailureConferenceEvent(
    override val at: Instant,
    val t: Throwable,
) : ConferenceEvent
