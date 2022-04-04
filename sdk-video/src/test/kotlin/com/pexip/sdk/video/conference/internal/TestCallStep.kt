package com.pexip.sdk.video.conference.internal

import com.pexip.sdk.api.Call
import com.pexip.sdk.api.infinity.InfinityService
import com.pexip.sdk.api.infinity.NewCandidateRequest
import com.pexip.sdk.api.infinity.UpdateRequest
import com.pexip.sdk.api.infinity.UpdateResponse

internal interface TestCallStep : InfinityService.CallStep {

    override fun newCandidate(request: NewCandidateRequest, token: String): Call<Unit> = TODO()

    override fun ack(token: String): Call<Unit> = TODO()

    override fun update(request: UpdateRequest, token: String): Call<UpdateResponse> = TODO()
}
