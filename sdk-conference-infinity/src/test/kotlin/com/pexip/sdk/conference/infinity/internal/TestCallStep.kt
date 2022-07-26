package com.pexip.sdk.conference.infinity.internal

import com.pexip.sdk.api.Call
import com.pexip.sdk.api.infinity.InfinityService
import com.pexip.sdk.api.infinity.NewCandidateRequest
import com.pexip.sdk.api.infinity.Token
import com.pexip.sdk.api.infinity.UpdateRequest
import com.pexip.sdk.api.infinity.UpdateResponse

internal abstract class TestCallStep : InfinityService.CallStep {

    override fun newCandidate(request: NewCandidateRequest, token: String): Call<Unit> = TODO()

    final override fun newCandidate(request: NewCandidateRequest, token: Token): Call<Unit> =
        newCandidate(request, token.token)

    override fun ack(token: String): Call<Unit> = TODO()

    final override fun ack(token: Token): Call<Unit> = ack(token.token)

    override fun update(request: UpdateRequest, token: String): Call<UpdateResponse> = TODO()

    final override fun update(request: UpdateRequest, token: Token): Call<UpdateResponse> =
        update(request, token.token)
}
