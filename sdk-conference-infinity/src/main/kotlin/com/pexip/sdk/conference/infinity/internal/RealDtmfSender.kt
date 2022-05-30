package com.pexip.sdk.conference.infinity.internal

import com.pexip.sdk.api.infinity.DtmfRequest
import com.pexip.sdk.api.infinity.InfinityService

internal class RealDtmfSender(
    private val store: TokenStore,
    private val participantStep: InfinityService.ParticipantStep,
) : DtmfSender {

    override fun send(digits: String) {
        val request = DtmfRequest(digits)
        participantStep.dtmf(request, store.get()).execute()
    }
}
