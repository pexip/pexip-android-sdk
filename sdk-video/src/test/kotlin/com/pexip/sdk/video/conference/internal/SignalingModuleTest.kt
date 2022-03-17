package com.pexip.sdk.video.conference.internal

import com.pexip.sdk.video.nextUuid
import java.util.concurrent.ExecutorService
import kotlin.random.Random
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class SignalingModuleTest {

    private lateinit var executorService: ExecutorService

    @BeforeTest
    fun setUp() {
        executorService = DirectExecutorService()
    }

    @Test
    fun `onOffer returns answer`() {
        val offer = "${Random.nextInt()}"
        val response = CallsResponse(
            call_uuid = Random.nextUuid(),
            sdp = "${Random.nextInt()}"
        )
        val infinityService = object : TestInfinityService {

            override fun calls(request: CallsRequest): CallsResponse {
                assertEquals("WEBRTC", request.call_type)
                assertEquals(offer, request.sdp)
                return response
            }
        }
        val module = SignalingModule(
            infinityService = infinityService,
            executorService = executorService,
            logger = ::println
        )
        var a: String? = null
        module.onOffer(offer) { answer -> a = answer }
        assertEquals(response.sdp, a)
        assertEquals(response.call_uuid, module.callId)
        module.dispose()
    }

    @Test
    fun `onIceCandidate sends the candidate`() {
        var r: CandidateRequest? = null
        val infinityService = object : TestInfinityService {

            override fun newCandidate(request: CandidateRequest) {
                r = request
            }
        }
        val module = SignalingModule(
            infinityService = infinityService,
            executorService = executorService,
            logger = ::println
        )
        val candidate = "${Random.nextInt()}"
        val mid = "audio"
        module.callId = Random.nextUuid()
        module.onIceCandidate(candidate, mid)
        assertEquals(module.callId, r?.callId)
        assertEquals(candidate, r?.candidate)
        assertEquals(mid, r?.mid)
        module.dispose()
    }

    @Test
    fun `onConnected sends ack`() {
        var r: AckRequest? = null
        val infinityService = object : TestInfinityService {

            override fun ack(request: AckRequest) {
                r = request
            }
        }
        val module = SignalingModule(
            infinityService = infinityService,
            executorService = executorService,
            logger = ::println
        )
        module.callId = Random.nextUuid()
        module.onConnected()
        assertEquals(module.callId, r?.callId)
        module.dispose()
    }

    @AfterTest
    fun tearDown() {
        assertTrue(executorService.isShutdown)
    }
}
