package com.pexip.sdk.video.internal

import kotlinx.coroutines.flow.Flow

internal interface InfinityService {

    val events: Flow<Event>

    fun refreshToken(): String

    fun releaseToken()

    fun calls(request: CallsRequest): CallsResponse

    fun ack(request: AckRequest)

    fun newCandidate(request: CandidateRequest)
}
