package com.pexip.sdk.conference.infinity.internal

import com.pexip.sdk.api.Call
import com.pexip.sdk.api.EventSourceFactory
import com.pexip.sdk.api.infinity.InfinityService
import com.pexip.sdk.api.infinity.MessageRequest
import com.pexip.sdk.api.infinity.RefreshTokenResponse
import com.pexip.sdk.api.infinity.RequestTokenRequest
import com.pexip.sdk.api.infinity.RequestTokenResponse
import java.util.UUID

internal interface TestConferenceStep : InfinityService.ConferenceStep {

    override fun requestToken(request: RequestTokenRequest): Call<RequestTokenResponse> = TODO()

    override fun requestToken(
        request: RequestTokenRequest,
        pin: String,
    ): Call<RequestTokenResponse> = TODO()

    override fun refreshToken(token: String): Call<RefreshTokenResponse> = TODO()

    override fun releaseToken(token: String): Call<Unit> = TODO()

    override fun message(request: MessageRequest, token: String): Call<Boolean> = TODO()

    override fun events(token: String): EventSourceFactory = TODO()

    override fun participant(participantId: UUID): InfinityService.ParticipantStep = TODO()
}
