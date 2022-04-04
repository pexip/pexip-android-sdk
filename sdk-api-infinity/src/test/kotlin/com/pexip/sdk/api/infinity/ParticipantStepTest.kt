package com.pexip.sdk.api.infinity

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockWebServer
import org.junit.Rule
import java.net.URL
import java.util.UUID
import kotlin.random.Random
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

internal class ParticipantStepTest {

    @get:Rule
    val server = MockWebServer()

    private lateinit var node: URL
    private lateinit var conferenceAlias: String
    private lateinit var participantId: UUID
    private lateinit var json: Json
    private lateinit var step: InfinityService.ParticipantStep

    @BeforeTest
    fun setUp() {
        node = server.url("/").toUrl()
        conferenceAlias = Random.nextString(8)
        participantId = UUID.randomUUID()
        json = Json { ignoreUnknownKeys = true }
        val service = InfinityService.create(OkHttpClient(), json)
        step = service.newRequest(node)
            .conference(conferenceAlias)
            .participant(participantId)
    }

    @Test
    fun `calls throws IllegalStateException`() {
        server.enqueue { setResponseCode(500) }
        val request = CallsRequest(
            sdp = Random.nextString(8),
            present = Random.nextString(8),
            callType = Random.nextString(8)
        )
        val token = Random.nextString(8)
        assertFailsWith<IllegalStateException> { step.calls(request, token).execute() }
        server.verifyCalls(request, token)
    }

    @Test
    fun `calls returns CallsResponse`() {
        val response = CallsResponse(
            callId = UUID.randomUUID(),
            sdp = Random.nextString(8)
        )
        server.enqueue {
            setResponseCode(200)
            setBody(json.encodeToString(Box(response)))
        }
        val request = CallsRequest(
            sdp = Random.nextString(8),
            present = Random.nextString(8),
            callType = Random.nextString(8)
        )
        val token = Random.nextString(8)
        assertEquals(response, step.calls(request, token).execute())
        server.verifyCalls(request, token)
    }

    private fun MockWebServer.verifyCalls(request: CallsRequest, token: String) = takeRequest {
        assertRequestUrl(node) {
            addPathSegments("api/client/v2")
            addPathSegment("conferences")
            addPathSegment(conferenceAlias)
            addPathSegment("participants")
            addPathSegment(participantId.toString())
            addPathSegment("calls")
        }
        assertToken(token)
        assertPost(json, request)
    }
}
