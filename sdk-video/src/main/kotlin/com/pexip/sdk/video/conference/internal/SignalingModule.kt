package com.pexip.sdk.video.conference.internal

import com.pexip.sdk.video.api.CallId
import com.pexip.sdk.video.api.CallsRequest
import com.pexip.sdk.video.api.ConferenceAlias
import com.pexip.sdk.video.api.InfinityService
import com.pexip.sdk.video.api.NewCandidateRequest
import com.pexip.sdk.video.api.Node
import com.pexip.sdk.video.api.ParticipantId
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

internal class SignalingModule(
    private val service: InfinityService,
    private val store: TokenStore,
    private val node: Node,
    private val conferenceAlias: ConferenceAlias,
    private val participantId: ParticipantId,
    private val executor: ExecutorService = Executors.newSingleThreadExecutor(),
    private val logger: Logger = Logger,
) : Disposable {

    @Volatile
    var callId: CallId? = null

    fun onConnected() {
        logger.log("onConnected()")
        executor.maybeSubmit {
            val callId = checkNotNull(callId) { "callId is not set." }
            service.newRequest(node)
                .conference(conferenceAlias)
                .participant(participantId)
                .call(callId)
                .ack(store.token)
                .execute()
        }
    }

    fun onIceCandidate(candidate: String, mid: String) {
        logger.log("onIceCandidate($candidate)")
        executor.maybeSubmit {
            val callId = checkNotNull(callId) { "callId is not set." }
            val request = NewCandidateRequest(candidate, mid)
            service.newRequest(node)
                .conference(conferenceAlias)
                .participant(participantId)
                .call(callId)
                .newCandidate(request, store.token)
                .execute()
        }
    }

    fun onOffer(offer: String, onAnswer: (String) -> Unit) {
        logger.log("onOffer($offer)")
        executor.maybeSubmit {
            val request = CallsRequest(offer)
            val response = service.newRequest(node)
                .conference(conferenceAlias)
                .participant(participantId)
                .calls(request, store.token)
                .execute()
            callId = response.callId
            onAnswer(response.sdp)
        }
    }

    override fun dispose() {
        executor.shutdownNow()
    }
}
