package com.pexip.sdk.video.internal

internal interface TestInfinityService : InfinityService {

    override fun refreshToken(): String = error("Not implemented.")

    override fun releaseToken(): Unit = error("Not implemented.")

    override fun calls(request: CallsRequest): CallsResponse = error("Not implemented.")

    override fun ack(request: AckRequest): Unit = error("Not implemented.")

    override fun newCandidate(request: CandidateRequest): Unit = error("Not implemented.")
}
