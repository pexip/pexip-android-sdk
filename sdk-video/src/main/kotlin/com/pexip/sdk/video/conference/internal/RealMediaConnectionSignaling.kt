package com.pexip.sdk.video.conference.internal

import com.pexip.sdk.api.infinity.CallsRequest
import com.pexip.sdk.api.infinity.InfinityService
import com.pexip.sdk.api.infinity.NewCandidateRequest
import com.pexip.sdk.api.infinity.UpdateRequest
import com.pexip.sdk.media.MediaConnectionSignaling

internal class RealMediaConnectionSignaling(
    private val store: TokenStore,
    private val participantStep: InfinityService.ParticipantStep,
) : MediaConnectionSignaling {

    var callStep: InfinityService.CallStep? by ThreadLocal()

    override fun onOffer(
        callType: String,
        description: String,
        presentationInMix: Boolean,
    ): String = when (val step = callStep) {
        null -> {
            val token = store.get()
            val request = CallsRequest(
                callType = callType,
                sdp = description,
                present = if (presentationInMix) "main" else null
            )
            val response = participantStep.calls(request, token).execute()
            callStep = participantStep.call(response.callId)
            response.sdp
        }
        else -> {
            val token = store.get()
            val request = UpdateRequest(description)
            val response = step.update(request, token).execute()
            response.sdp
        }
    }

    override fun onCandidate(candidate: String, mid: String) {
        val callStep = checkNotNull(callStep) { "callStep is not set" }
        val token = store.get()
        val request = NewCandidateRequest(candidate, mid)
        callStep.newCandidate(request, token).execute()
    }

    override fun onConnected() {
        val callStep = checkNotNull(callStep) { "callStep is not set" }
        val token = store.get()
        callStep.ack(token).execute()
    }
}
