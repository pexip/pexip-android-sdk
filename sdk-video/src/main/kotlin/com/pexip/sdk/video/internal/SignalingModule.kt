package com.pexip.sdk.video.internal

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

internal class SignalingModule(
    private val infinityService: InfinityService,
    private val executorService: ExecutorService = Executors.newSingleThreadExecutor(),
    private val logger: Logger = Logger,
) : Disposable {

    @Volatile
    var callId: String? = null

    fun onConnected() {
        logger.log("onConnected()")
        executorService.maybeSubmit {
            val callId = checkNotNull(callId) { "callId is not set." }
            val request = AckRequest(callId)
            infinityService.ack(request)
        }
    }

    fun onIceCandidate(candidate: String, mid: String) {
        logger.log("onIceCandidate($candidate)")
        executorService.maybeSubmit {
            val callId = checkNotNull(callId) { "callId is not set." }
            val request = CandidateRequest(
                callId = callId,
                candidate = candidate,
                mid = mid
            )
            infinityService.newCandidate(request)
        }
    }

    fun onOffer(offer: String, onAnswer: (String) -> Unit) {
        logger.log("onOffer($offer)")
        executorService.maybeSubmit {
            val request = CallsRequest(offer)
            val response = infinityService.calls(request)
            callId = response.call_uuid
            onAnswer(response.sdp)
        }
    }

    override fun dispose() {
        executorService.shutdownNow()
    }
}
