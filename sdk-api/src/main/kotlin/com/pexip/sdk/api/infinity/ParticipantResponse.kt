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
@file:UseSerializers(InstantComponentSerializer::class)

package com.pexip.sdk.api.infinity

import com.pexip.sdk.infinity.ParticipantId
import kotlinx.datetime.Instant
import kotlinx.datetime.serializers.InstantComponentSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

@Serializable
public data class ParticipantResponse(
    @SerialName("uuid")
    val id: ParticipantId,
    @SerialName("start_time")
    val startTime: Instant? = null,
    @SerialName("display_name")
    val displayName: String,
    @SerialName("overlay_text")
    val overlayText: String = displayName,
    @SerialName("is_presenting")
    val presenting: Boolean = false,
    @SerialName("is_muted")
    val audioMuted: Boolean = false,
    @SerialName("is_video_muted")
    val videoMuted: Boolean = false,
    @SerialName("mute_supported")
    val muteSupported: Boolean = false,
    @SerialName("transfer_supported")
    val transferSupported: Boolean = false,
    @SerialName("disconnect_supported")
    val disconnectSupported: Boolean = false,
    val role: Role = Role.UNKNOWN,
    @SerialName("service_type")
    val serviceType: ServiceType = ServiceType.UNKNOWN,
    @SerialName("buzz_time")
    val buzzTime: Instant? = null,
    @SerialName("spotlight")
    val spotlightTime: Instant? = null,
    @SerialName("call_tag")
    val callTag: String = "",
)
