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
package com.pexip.sdk.api.infinity

import com.pexip.sdk.api.Event
import com.pexip.sdk.api.infinity.internal.UUIDSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.util.UUID

@Serializable
public data class PresentationStartEvent(
    @SerialName("presenter_name")
    val presenterName: String,
    @Serializable(with = UUIDSerializer::class)
    @SerialName("presenter_uuid")
    val presenterId: UUID,
) : Event

public object PresentationStopEvent : Event {

    override fun toString(): String = "PresentationStopEvent"
}

@Serializable
public data class MessageReceivedEvent(
    @SerialName("origin")
    val participantName: String,
    @Serializable(with = UUIDSerializer::class)
    @SerialName("uuid")
    val participantId: UUID,
    val type: String,
    val payload: String,
    val direct: Boolean = false,
) : Event

@Serializable
public data class FeccEvent(
    public val action: FeccAction = FeccAction.UNKNOWN,
    public val timeout: Long,
    public val movement: List<FeccMovement>,
) : Event

@Serializable
public data class DisconnectEvent(val reason: String) : Event

public object ByeEvent : Event {

    override fun toString(): String = "ByeEvent"
}

@Serializable
public data class IncomingEvent(
    @SerialName("conference_alias")
    val conferenceAlias: String,
    @SerialName("remote_display_name")
    val remoteDisplayName: String,
    val token: String,
) : Event

@Serializable
public data class IncomingCancelledEvent(val token: String) : Event

internal fun Event(json: Json, id: String?, type: String?, data: String) = when (type) {
    "presentation_start" -> json.decodeFromString<PresentationStartEvent>(data)
    "presentation_stop" -> PresentationStopEvent
    "message_received" -> json.decodeFromString<MessageReceivedEvent>(data)
    "fecc" -> json.decodeFromString<FeccEvent>(data)
    "disconnect" -> json.decodeFromString<DisconnectEvent>(data)
    "bye" -> ByeEvent
    "incoming" -> json.decodeFromString<IncomingEvent>(data)
    "incoming_cancelled" -> json.decodeFromString<IncomingCancelledEvent>(data)
    else -> null
}