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
@file:UseSerializers(UUIDSerializer::class, ParticipantResponseSerializer::class)

package com.pexip.sdk.api.infinity

import com.pexip.sdk.api.Event
import com.pexip.sdk.api.infinity.internal.ParticipantResponseSerializer
import com.pexip.sdk.api.infinity.internal.UUIDSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.json.Json
import java.util.UUID

@Serializable
public data class ConferenceUpdateEvent(
    val locked: Boolean = false,
    val started: Boolean = false,
    @SerialName("guests_muted")
    val guestsMuted: Boolean = false,
    @SerialName("presentation_allowed")
    val presentationAllowed: Boolean = false,
) : Event

public data object ParticipantSyncBeginEvent : Event

public data object ParticipantSyncEndEvent : Event

@Serializable
@JvmInline
public value class ParticipantCreateEvent(public val response: ParticipantResponse) : Event

@Serializable
@JvmInline
public value class ParticipantUpdateEvent(public val response: ParticipantResponse) : Event

@Serializable
public data class ParticipantDeleteEvent(@SerialName("uuid") val id: UUID) : Event

@Serializable
public data class NewOfferEvent(val sdp: String) : Event

@Serializable
public data class UpdateSdpEvent(val sdp: String) : Event

@Serializable
public data class NewCandidateEvent(
    val candidate: String,
    val mid: String,
    val ufrag: String = "",
    val pwd: String = "",
) : Event

@Serializable
public data object PeerDisconnectEvent : Event

@Serializable
public data class PresentationStartEvent(
    @SerialName("presenter_name")
    val presenterName: String,
    @SerialName("presenter_uuid")
    val presenterId: UUID,
) : Event

public data object PresentationStopEvent : Event

@Serializable
public data class MessageReceivedEvent(
    @SerialName("origin")
    val participantName: String,
    @SerialName("uuid")
    val participantId: UUID,
    val type: String,
    val payload: String,
    val direct: Boolean = false,
) : Event

@Serializable
public data class LayoutEvent(
    @SerialName("view")
    val layout: LayoutId,
    @SerialName("requested_layout")
    val requestedLayout: RequestedLayout? = null,
    @SerialName("overlay_text_enabled")
    val overlayTextEnabled: Boolean = false,
) : Event

@Serializable
public data class FeccEvent(
    public val action: FeccAction = FeccAction.UNKNOWN,
    public val timeout: Long,
    public val movement: List<FeccMovement>,
) : Event

@Serializable
public data class ReferEvent(
    @SerialName("alias")
    val conferenceAlias: String,
    val token: String,
) : Event

@Serializable
public data class SplashScreenEvent(@SerialName("screen_key") val screenKey: String? = null) : Event

@Serializable
public data class DisconnectEvent(val reason: String) : Event

public data object ByeEvent : Event

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

@Suppress("ktlint:standard:function-naming")
internal fun Event(json: Json, id: String?, type: String?, data: String): Event? = when (type) {
    "conference_update" -> json.decodeFromString<ConferenceUpdateEvent>(data)
    "participant_sync_begin" -> ParticipantSyncBeginEvent
    "participant_sync_end" -> ParticipantSyncEndEvent
    "participant_create" -> json.decodeFromString<ParticipantCreateEvent>(data)
    "participant_update" -> json.decodeFromString<ParticipantUpdateEvent>(data)
    "participant_delete" -> json.decodeFromString<ParticipantDeleteEvent>(data)
    "new_offer" -> json.decodeFromString<NewOfferEvent>(data)
    "update_sdp" -> json.decodeFromString<UpdateSdpEvent>(data)
    "new_candidate" -> json.decodeFromString<NewCandidateEvent>(data)
    "peer_disconnect" -> PeerDisconnectEvent
    "presentation_start" -> json.decodeFromString<PresentationStartEvent>(data)
    "presentation_stop" -> PresentationStopEvent
    "message_received" -> json.decodeFromString<MessageReceivedEvent>(data)
    "layout" -> json.decodeFromString<LayoutEvent>(data)
    "fecc" -> json.decodeFromString<FeccEvent>(data)
    "refer" -> json.decodeFromString<ReferEvent>(data)
    "splash_screen" -> try {
        json.decodeFromString<SplashScreenEvent>(data)
    } catch (e: SerializationException) {
        SplashScreenEvent()
    }
    "disconnect" -> json.decodeFromString<DisconnectEvent>(data)
    "bye" -> ByeEvent
    "incoming" -> json.decodeFromString<IncomingEvent>(data)
    "incoming_cancelled" -> json.decodeFromString<IncomingCancelledEvent>(data)
    else -> null
}
