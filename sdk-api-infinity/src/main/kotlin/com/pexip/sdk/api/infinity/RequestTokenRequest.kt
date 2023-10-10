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

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class RequestTokenRequest(
    @SerialName("conference_extension")
    public val conferenceExtension: String? = null,
    @SerialName("display_name")
    public val displayName: String? = null,
    @SerialName("chosen_idp")
    public val chosenIdp: IdentityProviderId? = null,
    @SerialName("sso_token")
    public val ssoToken: String? = null,
    @SerialName("token")
    public val incomingToken: String? = null,
    @SerialName("registration_token")
    public val registrationToken: String? = null,
    @SerialName("direct_media")
    public val directMedia: Boolean = false,
)
