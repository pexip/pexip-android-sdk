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

import com.pexip.sdk.api.infinity.internal.DurationAsMillisecondsSerializer
import com.pexip.sdk.api.infinity.internal.UUIDSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.builtins.LongAsStringSerializer
import java.util.UUID
import kotlin.time.Duration

@Serializable
public data class RequestTokenResponse(
    override val token: String,
    @Serializable(with = LongAsStringSerializer::class)
    override val expires: Long,
    @SerialName("conference_name")
    public val conferenceName: String,
    @Serializable(with = UUIDSerializer::class)
    @SerialName("participant_uuid")
    public val participantId: UUID,
    @SerialName("display_name")
    public val participantName: String,
    public val version: VersionResponse,
    @SerialName("analytics_enabled")
    public val analyticsEnabled: Boolean = false,
    @SerialName("chat_enabled")
    public val chatEnabled: Boolean = false,
    @SerialName("guests_can_present")
    public val guestsCanPresent: Boolean = false,
    @SerialName("service_type")
    public val serviceType: ServiceType = ServiceType.UNKNOWN,
    public val stun: List<StunResponse> = emptyList(),
    public val turn: List<TurnResponse> = emptyList(),
    @SerialName("direct_media")
    public val directMedia: Boolean = false,
    @SerialName("use_relay_candidates_only")
    public val useRelayCandidatesOnly: Boolean = false,
    @SerialName("pex_datachannel_id")
    public val dataChannelId: Int? = null,
    @Serializable(with = DurationAsMillisecondsSerializer::class)
    @SerialName("client_stats_update_interval")
    public val clientStatsUpdateInterval: Duration = Duration.INFINITE,
    @Transient
    public val directMediaRequested: Boolean = false,
) : Token
