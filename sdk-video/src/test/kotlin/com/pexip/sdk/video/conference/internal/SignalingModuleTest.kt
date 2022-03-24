package com.pexip.sdk.video.conference.internal

import com.pexip.sdk.video.api.Call
import com.pexip.sdk.video.api.CallsRequest
import com.pexip.sdk.video.api.CallsResponse
import com.pexip.sdk.video.api.ConferenceAlias
import com.pexip.sdk.video.api.NewCandidateRequest
import com.pexip.sdk.video.api.Node
import com.pexip.sdk.video.api.Node.Companion.toNode
import com.pexip.sdk.video.api.ParticipantId
import com.pexip.sdk.video.api.RecordingInfinityService
import com.pexip.sdk.video.api.TestCall
import com.pexip.sdk.video.api.nextCallId
import com.pexip.sdk.video.api.nextConferenceAlias
import com.pexip.sdk.video.api.nextParticipantId
import com.pexip.sdk.video.nextToken
import java.util.concurrent.ExecutorService
import kotlin.properties.Delegates
import kotlin.random.Random
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.minutes

internal class SignalingModuleTest {

    private lateinit var store: TokenStore
    private lateinit var executor: ExecutorService

    private var node: Node by Delegates.notNull()
    private var conferenceAlias: ConferenceAlias by Delegates.notNull()
    private var participantId: ParticipantId by Delegates.notNull()

    @BeforeTest
    fun setUp() {
        store = TokenStore(Random.nextToken(), 2.minutes)
        node = "http://example.com".toNode()
        conferenceAlias = Random.nextConferenceAlias()
        participantId = Random.nextParticipantId()
        executor = DirectExecutorService()
    }

    @AfterTest
    fun tearDown() {
        assertTrue(executor.isShutdown)
    }

    @Test
    fun `onOffer returns answer`() {
        val offer = "${Random.nextInt()}"
        val response = CallsResponse(
            callId = Random.nextCallId(),
            sdp = "${Random.nextInt()}"
        )
        val builder = object : RecordingInfinityService.RequestBuilder() {

            override fun calls(
                request: CallsRequest,
                token: String,
            ): Call<CallsResponse> = object : TestCall<CallsResponse> {

                override fun execute(): CallsResponse {
                    assertEquals(offer, request.sdp)
                    assertEquals(store.token, token)
                    return response
                }
            }
        }
        val service = RecordingInfinityService(builder)
        val module = SignalingModule(
            service = service,
            store = store,
            node = node,
            conferenceAlias = conferenceAlias,
            participantId = participantId,
            executor = executor,
            logger = ::println
        )
        var a: String? = null
        module.onOffer(offer) { answer -> a = answer }
        assertEquals(node, builder.node)
        assertEquals(conferenceAlias, builder.conferenceAlias)
        assertEquals(participantId, builder.participantId)
        assertEquals(response.sdp, a)
        assertEquals(response.callId, module.callId)
        module.dispose()
    }

    @Test
    fun `onIceCandidate sends the candidate`() {
        val candidate = "${Random.nextInt()}"
        val mid = "audio"
        val builder = object : RecordingInfinityService.RequestBuilder() {

            override fun newCandidate(
                request: NewCandidateRequest,
                token: String,
            ): Call<Unit> = object : TestCall<Unit> {

                override fun execute() {
                    assertEquals(candidate, request.candidate)
                    assertEquals(mid, request.mid)
                    assertEquals(store.token, token)
                }
            }
        }
        val service = RecordingInfinityService(builder)
        val module = SignalingModule(
            service = service,
            store = store,
            node = node,
            executor = executor,
            conferenceAlias = conferenceAlias,
            participantId = participantId,
            logger = ::println
        )
        module.callId = Random.nextCallId()
        module.onIceCandidate(candidate, mid)
        assertEquals(node, builder.node)
        assertEquals(conferenceAlias, builder.conferenceAlias)
        assertEquals(participantId, builder.participantId)
        assertEquals(module.callId, builder.callId)
        module.dispose()
    }

    @Test
    fun `onConnected sends ack`() {
        val builder = object : RecordingInfinityService.RequestBuilder() {

            override fun ack(token: String): Call<Unit> =
                object : TestCall<Unit> {

                    override fun execute() {
                        assertEquals(store.token, token)
                    }
                }
        }
        val service = RecordingInfinityService(builder)
        val module = SignalingModule(
            service = service,
            store = store,
            node = node,
            conferenceAlias = conferenceAlias,
            participantId = participantId,
            executor = executor,
            logger = ::println
        )
        module.callId = Random.nextCallId()
        module.onConnected()
        assertEquals(node, builder.node)
        assertEquals(conferenceAlias, builder.conferenceAlias)
        assertEquals(participantId, builder.participantId)
        assertEquals(module.callId, builder.callId)
        module.dispose()
    }
}
