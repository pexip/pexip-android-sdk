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
package com.pexip.sdk.conference.infinity.internal

import com.pexip.sdk.api.Call
import com.pexip.sdk.api.EventSourceFactory
import com.pexip.sdk.api.infinity.InfinityService
import com.pexip.sdk.api.infinity.MessageRequest
import com.pexip.sdk.api.infinity.RefreshTokenResponse
import com.pexip.sdk.api.infinity.RequestTokenRequest
import com.pexip.sdk.api.infinity.RequestTokenResponse
import com.pexip.sdk.api.infinity.Token
import java.util.UUID

internal abstract class TestConferenceStep : InfinityService.ConferenceStep {

    override val requestBuilder: InfinityService.RequestBuilder get() = TODO()

    override fun requestToken(request: RequestTokenRequest): Call<RequestTokenResponse> = TODO()

    override fun requestToken(
        request: RequestTokenRequest,
        pin: String,
    ): Call<RequestTokenResponse> = TODO()

    override fun refreshToken(token: String): Call<RefreshTokenResponse> = TODO()

    final override fun refreshToken(token: Token): Call<RefreshTokenResponse> =
        refreshToken(token.token)

    override fun releaseToken(token: String): Call<Boolean> = TODO()

    final override fun releaseToken(token: Token): Call<Boolean> = releaseToken(token.token)

    override fun message(request: MessageRequest, token: String): Call<Boolean> = TODO()

    final override fun message(request: MessageRequest, token: Token): Call<Boolean> =
        message(request, token.token)

    override fun events(token: String): EventSourceFactory = TODO()

    override fun events(token: Token): EventSourceFactory = events(token.token)

    override fun participant(participantId: UUID): InfinityService.ParticipantStep = TODO()
}
