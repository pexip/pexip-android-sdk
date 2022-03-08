package com.pexip.sdk.video.internal

internal interface InfinityService {

    fun refreshToken(): String

    fun releaseToken()

    fun calls(request: CallsRequest): CallsResponse

    fun ack(request: AckRequest)

    fun newCandidate(request: CandidateRequest)
}
