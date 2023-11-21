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
package com.pexip.sdk.registration.infinity.internal

import com.pexip.sdk.api.Call
import com.pexip.sdk.api.EventSourceFactory
import com.pexip.sdk.api.infinity.InfinityService
import com.pexip.sdk.api.infinity.RefreshRegistrationTokenResponse
import com.pexip.sdk.api.infinity.RegistrationResponse
import com.pexip.sdk.api.infinity.RequestRegistrationTokenResponse
import com.pexip.sdk.api.infinity.Token

internal abstract class TestRegistrationStep : InfinityService.RegistrationStep {

    override val requestBuilder: InfinityService.RequestBuilder get() = TODO()

    override fun requestToken(
        username: String,
        password: String,
    ): Call<RequestRegistrationTokenResponse> = TODO()

    override fun refreshToken(token: String): Call<RefreshRegistrationTokenResponse> =
        TODO()

    override fun refreshToken(token: Token): Call<RefreshRegistrationTokenResponse> =
        refreshToken(token.token)

    override fun releaseToken(token: String): Call<Boolean> = TODO()

    override fun releaseToken(token: Token): Call<Boolean> = releaseToken(token.token)

    override fun events(token: String): EventSourceFactory = TODO()

    override fun events(token: Token): EventSourceFactory = events(token.token)

    override fun registrations(token: String, query: String): Call<List<RegistrationResponse>> =
        TODO()

    override fun registrations(token: Token, query: String): Call<List<RegistrationResponse>> =
        registrations(token.token, query)
}
