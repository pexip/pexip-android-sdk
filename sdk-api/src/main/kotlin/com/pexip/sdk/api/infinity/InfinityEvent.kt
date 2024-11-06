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
@file:UseSerializers(ParticipantResponseSerializer::class)

package com.pexip.sdk.api.infinity

import com.pexip.sdk.api.Event
import com.pexip.sdk.api.infinity.internal.DurationAsMillisecondsSerializer
import com.pexip.sdk.api.infinity.internal.ParticipantResponseSerializer
import com.pexip.sdk.infinity.BreakoutId
import com.pexip.sdk.infinity.LayoutId
import com.pexip.sdk.infinity.ParticipantId
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.json.JsonClassDiscriminator
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.put
import kotlin.time.Duration

@Serializable
@OptIn(ExperimentalSerializationApi::class)
@JsonClassDiscriminator("#event")
public sealed interface InfinityEvent : Event

@Serializable
public data object UnknownEvent : InfinityEvent

@Serializable
@SerialName("conference_update")
public data class ConferenceUpdateEvent(
    val locked: Boolean = false,
    val started: Boolean = false,
    @SerialName("guests_muted")
    val guestsMuted: Boolean = false,
    @SerialName("presentation_allowed")
    val presentationAllowed: Boolean = false,
    @SerialName("guests_can_unmute")
    val guestsCanUnmute: Boolean? = null,
) : InfinityEvent

@Serializable
@SerialName("stage")
public data class StageEvent(@SerialName("data") val speakers: List<SpeakerResponse>) :
    InfinityEvent {

    public constructor(vararg speakers: SpeakerResponse) : this(speakers.asList())
}

@Serializable
@SerialName("participant_sync_begin")
public data object ParticipantSyncBeginEvent : InfinityEvent

@Serializable
@SerialName("participant_sync_end")
public data object ParticipantSyncEndEvent : InfinityEvent

@JvmInline
@Serializable
@SerialName("participant_create")
public value class ParticipantCreateEvent(public val response: ParticipantResponse) : InfinityEvent

@JvmInline
@Serializable
@SerialName("participant_update")
public value class ParticipantUpdateEvent(public val response: ParticipantResponse) : InfinityEvent

@Serializable
@SerialName("participant_delete")
public data class ParticipantDeleteEvent(@SerialName("uuid") val id: ParticipantId) : InfinityEvent

@Serializable
@SerialName("new_offer")
public data class NewOfferEvent(val sdp: String) : InfinityEvent

@Serializable
@SerialName("update_sdp")
public data class UpdateSdpEvent(val sdp: String) : InfinityEvent

@Serializable
@SerialName("new_candidate")
public data class NewCandidateEvent(
    val candidate: String,
    val mid: String,
    val ufrag: String = "",
    val pwd: String = "",
) : InfinityEvent

@Serializable
@SerialName("peer_disconnect")
public data object PeerDisconnectEvent : InfinityEvent

@Serializable
@SerialName("presentation_start")
public data class PresentationStartEvent(
    @SerialName("presenter_name")
    val presenterName: String,
    @SerialName("presenter_uuid")
    val presenterId: ParticipantId,
) : InfinityEvent

@Serializable
@SerialName("presentation_stop")
public data object PresentationStopEvent : InfinityEvent

@Serializable
@SerialName("message_received")
public data class MessageReceivedEvent(
    @SerialName("origin")
    val participantName: String,
    @SerialName("uuid")
    val participantId: ParticipantId,
    val type: String,
    val payload: String,
    val direct: Boolean = false,
) : InfinityEvent

@Serializable
@SerialName("layout")
public data class LayoutEvent(
    @SerialName("view")
    val layout: LayoutId,
    @SerialName("requested_layout")
    val requestedLayout: RequestedLayout? = null,
    @SerialName("overlay_text_enabled")
    val overlayTextEnabled: Boolean = false,
) : InfinityEvent

@Serializable
@SerialName("fecc")
public data class FeccEvent(
    public val action: FeccAction = FeccAction.UNKNOWN,
    @Serializable(with = DurationAsMillisecondsSerializer::class)
    public val timeout: Duration,
    public val movement: List<FeccMovement>,
) : InfinityEvent

@Serializable
@SerialName("refer")
public data class ReferEvent(
    @SerialName("alias")
    val conferenceAlias: String,
    val token: String,
) : InfinityEvent

@Serializable
@SerialName("splash_screen")
public data class SplashScreenEvent(@SerialName("screen_key") val screenKey: String? = null) :
    InfinityEvent

@Serializable
@SerialName("breakout_begin")
public data class BreakoutBeginEvent(
    @SerialName("breakout_uuid") val id: BreakoutId,
    @SerialName("participant_uuid") val participantId: ParticipantId,
) : InfinityEvent

@Serializable
@SerialName("breakout_end")
public data class BreakoutEndEvent(
    @SerialName("breakout_uuid") val id: BreakoutId,
    @SerialName("participant_uuid") val participantId: ParticipantId,
) : InfinityEvent

@Serializable
@SerialName("disconnect")
public data class DisconnectEvent(val reason: String) : InfinityEvent

@Serializable
@SerialName("bye")
public data object ByeEvent : InfinityEvent

@Serializable
@SerialName("incoming")
public data class IncomingEvent(
    @SerialName("conference_alias")
    val conferenceAlias: String,
    @SerialName("remote_display_name")
    val remoteDisplayName: String,
    val token: String,
) : InfinityEvent

@Serializable
@SerialName("incoming_cancelled")
public data class IncomingCancelledEvent(val token: String) : InfinityEvent

@Suppress("ktlint:standard:function-naming")
internal fun InfinityEvent(
    @Suppress("UNUSED_PARAMETER") id: String?,
    type: String?,
    data: String,
): InfinityEvent {
    type?.takeIf(String::isNotBlank) ?: return UnknownEvent
    val d = InfinityService.Json.decodeFromString<JsonElement>(data)
    val o = buildJsonObject {
        put("#event", type)
        when (d) {
            is JsonObject -> d.forEach { (key, value) -> put(key, value) }
            else -> put("data", d)
        }
    }
    return InfinityService.Json.decodeFromJsonElement<InfinityEvent>(o)
}
