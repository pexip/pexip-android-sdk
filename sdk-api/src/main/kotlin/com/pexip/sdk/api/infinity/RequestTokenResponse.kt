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
package com.pexip.sdk.api.infinity

import com.pexip.sdk.api.infinity.internal.DurationAsMillisecondsSerializer
import com.pexip.sdk.api.infinity.internal.DurationAsSecondsStringSerializer
import com.pexip.sdk.infinity.ParticipantId
import com.pexip.sdk.infinity.ServiceType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlin.time.Duration

@Serializable
public data class RequestTokenResponse(
    override val token: String,
    @Serializable(with = DurationAsSecondsStringSerializer::class)
    override val expires: Duration,
    @SerialName("conference_name")
    val conferenceName: String,
    @SerialName("participant_uuid")
    val participantId: ParticipantId,
    @SerialName("display_name")
    val participantName: String,
    val version: VersionResponse,
    @SerialName("analytics_enabled")
    val analyticsEnabled: Boolean = false,
    @SerialName("chat_enabled")
    val chatEnabled: Boolean = false,
    @SerialName("guests_can_present")
    val guestsCanPresent: Boolean = false,
    @SerialName("service_type")
    val serviceType: ServiceType = ServiceType.UNKNOWN,
    val stun: List<StunResponse> = emptyList(),
    val turn: List<TurnResponse> = emptyList(),
    @SerialName("direct_media")
    val directMedia: Boolean = false,
    @SerialName("use_relay_candidates_only")
    val useRelayCandidatesOnly: Boolean = false,
    @SerialName("pex_datachannel_id")
    val dataChannelId: Int = -1,
    @Serializable(with = DurationAsMillisecondsSerializer::class)
    @SerialName("client_stats_update_interval")
    val clientStatsUpdateInterval: Duration = Duration.INFINITE,
    @SerialName("call_tag")
    val callTag: String = "",
    @SerialName("parent_participant_uuid")
    val parentParticipantId: ParticipantId? = null,
    @Transient
    val directMediaRequested: Boolean = false,
) : Token
