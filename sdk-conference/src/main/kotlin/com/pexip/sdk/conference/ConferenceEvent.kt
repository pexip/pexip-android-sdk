/*
 * Copyright 2022-2023 Pexip AS
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

import java.util.UUID

public sealed interface ConferenceEvent {

    public val at: Long
}

public data class PresentationStartConferenceEvent(
    override val at: Long,
    val presenterId: UUID,
    val presenterName: String,
) : ConferenceEvent

public data class PresentationStopConferenceEvent(override val at: Long) : ConferenceEvent

@Deprecated("Use Messenger to receive messages instead.")
public data class MessageReceivedConferenceEvent(
    override val at: Long,
    val participantId: UUID,
    val participantName: String,
    val type: String,
    val payload: String,
) : ConferenceEvent

public data class ReferConferenceEvent(
    override val at: Long,
    val conferenceAlias: String,
    val token: String,
) : ConferenceEvent

public data class DisconnectConferenceEvent(
    override val at: Long,
    val reason: String,
) : ConferenceEvent

public data class FailureConferenceEvent(override val at: Long, val t: Throwable) : ConferenceEvent
