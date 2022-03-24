package com.pexip.sdk.video.api

internal class RecordingInfinityService(private val builder: RequestBuilder) :
    InfinityService {

    override fun newRequest(node: Node): InfinityService.RequestBuilder {
        builder.node = node
        return builder
    }

    abstract class RequestBuilder :
        InfinityService.RequestBuilder,
        InfinityService.ConferenceStep,
        InfinityService.ParticipantStep,
        InfinityService.CallStep {

        var node: Node? = null
        var conferenceAlias: ConferenceAlias? = null
        var participantId: ParticipantId? = null
        var callId: CallId? = null

        final override fun conference(conferenceAlias: ConferenceAlias): InfinityService.ConferenceStep =
            apply {
                this.conferenceAlias = conferenceAlias
            }

        override fun status(): Call<Boolean> = TODO()

        final override fun participant(participantId: ParticipantId): InfinityService.ParticipantStep =
            apply {
                this.participantId = participantId
            }

        override fun requestToken(request: RequestTokenRequest): Call<RequestTokenResponse> = TODO()

        override fun requestToken(
            request: RequestTokenRequest,
            pin: String,
        ): Call<RequestTokenResponse> = TODO()

        override fun refreshToken(token: String): Call<RefreshTokenResponse> =
            TODO()

        override fun releaseToken(token: String): Call<Unit> = TODO()

        override fun calls(
            request: CallsRequest,
            token: String,
        ): Call<CallsResponse> = TODO()

        final override fun call(callId: CallId): InfinityService.CallStep = apply {
            this.callId = callId
        }

        override fun newCandidate(
            request: NewCandidateRequest,
            token: String,
        ): Call<Unit> = TODO()

        override fun ack(token: String): Call<Unit> = TODO()
    }
}
