package com.pexip.sdk.conference.infinity.internal

import com.pexip.sdk.api.Call
import com.pexip.sdk.api.infinity.CallsRequest
import com.pexip.sdk.api.infinity.CallsResponse
import com.pexip.sdk.api.infinity.InfinityService
import java.util.UUID

internal interface TestParticipantTest : InfinityService.ParticipantStep {

    override fun calls(request: CallsRequest, token: String): Call<CallsResponse> = TODO()

    override fun mute(token: String): Call<Unit> = TODO()

    override fun unmute(token: String): Call<Unit> = TODO()

    override fun videoMuted(token: String): Call<Unit> = TODO()

    override fun videoUnmuted(token: String): Call<Unit> = TODO()

    override fun call(callId: UUID): InfinityService.CallStep = TODO()
}
