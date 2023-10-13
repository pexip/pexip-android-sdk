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
import com.pexip.sdk.api.infinity.AckRequest
import com.pexip.sdk.api.infinity.DtmfRequest
import com.pexip.sdk.api.infinity.InfinityService
import com.pexip.sdk.api.infinity.NewCandidateRequest
import com.pexip.sdk.api.infinity.Token
import com.pexip.sdk.api.infinity.UpdateRequest
import com.pexip.sdk.api.infinity.UpdateResponse

internal abstract class TestCallStep : InfinityService.CallStep {

    override val participantStep: InfinityService.ParticipantStep get() = TODO()

    override fun newCandidate(request: NewCandidateRequest, token: String): Call<Unit> = TODO()

    final override fun newCandidate(request: NewCandidateRequest, token: Token): Call<Unit> =
        newCandidate(request, token.token)

    override fun ack(token: String): Call<Unit> = TODO()

    final override fun ack(token: Token): Call<Unit> = ack(token.token)

    override fun ack(request: AckRequest, token: String): Call<Unit> = TODO()

    final override fun ack(request: AckRequest, token: Token): Call<Unit> =
        ack(request, token.token)

    override fun update(request: UpdateRequest, token: String): Call<UpdateResponse> = TODO()

    final override fun update(request: UpdateRequest, token: Token): Call<UpdateResponse> =
        update(request, token.token)

    override fun dtmf(request: DtmfRequest, token: String): Call<Boolean> = TODO()

    final override fun dtmf(request: DtmfRequest, token: Token): Call<Boolean> =
        dtmf(request, token.token)
}
