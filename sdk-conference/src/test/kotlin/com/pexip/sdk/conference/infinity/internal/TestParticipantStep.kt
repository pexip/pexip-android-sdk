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
package com.pexip.sdk.conference.infinity.internal

import com.pexip.sdk.api.Call
import com.pexip.sdk.api.infinity.CallsRequest
import com.pexip.sdk.api.infinity.CallsResponse
import com.pexip.sdk.api.infinity.DtmfRequest
import com.pexip.sdk.api.infinity.InfinityService
import com.pexip.sdk.api.infinity.MessageRequest
import com.pexip.sdk.api.infinity.PreferredAspectRatioRequest
import com.pexip.sdk.api.infinity.Token
import java.util.UUID

internal open class TestParticipantStep : InfinityService.ParticipantStep {

    override val conferenceStep: InfinityService.ConferenceStep get() = TODO()

    override fun calls(request: CallsRequest, token: String): Call<CallsResponse> = TODO()

    final override fun calls(request: CallsRequest, token: Token): Call<CallsResponse> =
        calls(request, token.token)

    override fun dtmf(request: DtmfRequest, token: String): Call<Boolean> = TODO()

    final override fun dtmf(request: DtmfRequest, token: Token): Call<Boolean> =
        dtmf(request, token.token)

    override fun mute(token: String): Call<Unit> = TODO()

    final override fun mute(token: Token): Call<Unit> = mute(token.token)

    override fun unmute(token: String): Call<Unit> = TODO()

    final override fun unmute(token: Token): Call<Unit> = unmute(token.token)

    override fun videoMuted(token: String): Call<Unit> = TODO()

    final override fun videoMuted(token: Token): Call<Unit> = videoMuted(token.token)

    override fun videoUnmuted(token: String): Call<Unit> = TODO()

    final override fun videoUnmuted(token: Token): Call<Unit> = videoUnmuted(token.token)

    override fun takeFloor(token: String): Call<Unit> = TODO()

    final override fun takeFloor(token: Token): Call<Unit> = takeFloor(token.token)

    override fun releaseFloor(token: String): Call<Unit> = TODO()

    final override fun releaseFloor(token: Token): Call<Unit> = releaseFloor(token.token)

    override fun message(request: MessageRequest, token: String): Call<Boolean> = TODO()

    final override fun message(request: MessageRequest, token: Token): Call<Boolean> =
        message(request, token.token)

    override fun preferredAspectRatio(
        request: PreferredAspectRatioRequest,
        token: String,
    ): Call<Boolean> = TODO()

    final override fun preferredAspectRatio(
        request: PreferredAspectRatioRequest,
        token: Token,
    ): Call<Boolean> = preferredAspectRatio(request, token.token)

    override fun buzz(token: String): Call<Boolean> = TODO()

    override fun buzz(token: Token): Call<Boolean> = buzz(token.token)

    override fun clearBuzz(token: String): Call<Boolean> = TODO()

    override fun clearBuzz(token: Token): Call<Boolean> = clearBuzz(token.token)

    override fun call(callId: UUID): InfinityService.CallStep = TODO()
}
