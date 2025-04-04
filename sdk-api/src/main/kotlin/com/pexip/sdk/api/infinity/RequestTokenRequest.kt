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

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@OptIn(ExperimentalSerializationApi::class)
@Serializable
public data class RequestTokenRequest(
    @SerialName("conference_extension")
    val conferenceExtension: String? = null,
    @SerialName("display_name")
    val displayName: String? = null,
    @SerialName("chosen_idp")
    val chosenIdp: IdentityProviderId? = null,
    @SerialName("sso_token")
    val ssoToken: String? = null,
    @SerialName("token")
    val incomingToken: String? = null,
    @SerialName("registration_token")
    val registrationToken: String? = null,
    @SerialName("direct_media")
    val directMedia: Boolean = false,
    @SerialName("call_tag")
    val callTag: String = "",
    @EncodeDefault
    @SerialName("breakout_capable")
    val breakoutCapable: Boolean = false,
)
