package com.pexip.sdk.api.infinity

import com.pexip.sdk.api.infinity.internal.addPathSegment
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

internal class CallStepTest {

    @get:Rule
    val server = MockWebServer()

    private lateinit var node: URL
    private lateinit var conferenceAlias: String
    private lateinit var participantId: UUID
    private lateinit var callId: UUID
    private lateinit var json: Json
    private lateinit var step: InfinityService.CallStep

    @BeforeTest
    fun setUp() {
        node = server.url("/").toUrl()
        conferenceAlias = Random.nextString(8)
        participantId = UUID.randomUUID()
        callId = UUID.randomUUID()
        json = Json { ignoreUnknownKeys = true }
        val service = InfinityService.create(OkHttpClient(), json)
        step = service.newRequest(node)
            .conference(conferenceAlias)
            .participant(participantId)
            .call(callId)
    }

    @Test
    fun `newCandidate throws IllegalStateException`() {
        server.enqueue { setResponseCode(500) }
        val request = Random.nextNewCandidateRequest()
        val token = Random.nextString(8)
        assertFailsWith<IllegalStateException> { step.newCandidate(request, token).execute() }
        server.verifyNewCandidate(request, token)
    }

    @Test
    fun `newCandidate throws NoSuchNodeException`() {
        server.enqueue { setResponseCode(404) }
        val request = Random.nextNewCandidateRequest()
        val token = Random.nextString(8)
        assertFailsWith<NoSuchNodeException> { step.newCandidate(request, token).execute() }
        server.verifyNewCandidate(request, token)
    }

    @Test
    fun `newCandidate throws NoSuchConferenceException`() {
        val message = "Neither conference nor gateway found"
        server.enqueue {
            setResponseCode(404)
            setBody(json.encodeToString(Box(message)))
        }
        val request = Random.nextNewCandidateRequest()
        val token = Random.nextString(8)
        assertFailsWith<NoSuchConferenceException> { step.newCandidate(request, token).execute() }
        server.verifyNewCandidate(request, token)
    }

    @Test
    fun `newCandidate throws InvalidTokenException`() {
        val message = "Invalid token"
        server.enqueue {
            setResponseCode(403)
            setBody(json.encodeToString(Box(message)))
        }
        val request = Random.nextNewCandidateRequest()
        val token = Random.nextString(8)
        assertFailsWith<InvalidTokenException> { step.newCandidate(request, token).execute() }
        server.verifyNewCandidate(request, token)
    }

    @Test
    fun `newCandidate returns on 200`() {
        server.enqueue { setResponseCode(200) }
        val request = Random.nextNewCandidateRequest()
        val token = Random.nextString(8)
        step.newCandidate(request, token).execute()
        server.verifyNewCandidate(request, token)
    }

    @Test
    fun `ack throws IllegalStateException`() {
        server.enqueue { setResponseCode(500) }
        val token = Random.nextString(8)
        assertFailsWith<IllegalStateException> { step.ack(token).execute() }
        server.verifyAck(token)
    }

    @Test
    fun `ack throws NoSuchNodeException`() {
        server.enqueue { setResponseCode(404) }
        val token = Random.nextString(8)
        assertFailsWith<NoSuchNodeException> { step.ack(token).execute() }
        server.verifyAck(token)
    }

    @Test
    fun `ack throws NoSuchConferenceException`() {
        val message = "Neither conference nor gateway found"
        server.enqueue {
            setResponseCode(404)
            setBody(json.encodeToString(Box(message)))
        }
        val token = Random.nextString(8)
        assertFailsWith<NoSuchConferenceException> { step.ack(token).execute() }
        server.verifyAck(token)
    }

    @Test
    fun `ack throws InvalidTokenException`() {
        val message = "Invalid token"
        server.enqueue {
            setResponseCode(403)
            setBody(json.encodeToString(Box(message)))
        }
        val token = Random.nextString(8)
        assertFailsWith<InvalidTokenException> { step.ack(token).execute() }
        server.verifyAck(token)
    }

    @Test
    fun `ack returns on 200`() {
        server.enqueue { setResponseCode(200) }
        val token = Random.nextString(8)
        step.ack(token).execute()
        server.verifyAck(token)
    }

    @Test
    fun `update throws IllegalStateException`() {
        server.enqueue { setResponseCode(500) }
        val token = Random.nextString(8)
        val request = Random.nextUpdateRequest()
        assertFailsWith<IllegalStateException> { step.update(request, token).execute() }
        server.verifyUpdate(request, token)
    }

    @Test
    fun `update throws NoSuchNodeException`() {
        server.enqueue { setResponseCode(404) }
        val token = Random.nextString(8)
        val request = Random.nextUpdateRequest()
        assertFailsWith<NoSuchNodeException> { step.update(request, token).execute() }
        server.verifyUpdate(request, token)
    }

    @Test
    fun `update throws NoSuchConferenceException`() {
        val message = "Neither conference nor gateway found"
        server.enqueue {
            setResponseCode(404)
            setBody(json.encodeToString(Box(message)))
        }
        val token = Random.nextString(8)
        val request = Random.nextUpdateRequest()
        val e = assertFailsWith<NoSuchConferenceException> { step.update(request, token).execute() }
        assertEquals(message, e.message)
        server.verifyUpdate(request, token)
    }

    @Test
    fun `update throws InvalidTokenException`() {
        val message = "Invalid token"
        server.enqueue {
            setResponseCode(403)
            setBody(json.encodeToString(Box(message)))
        }
        val token = Random.nextString(8)
        val request = Random.nextUpdateRequest()
        val e = assertFailsWith<InvalidTokenException> { step.update(request, token).execute() }
        assertEquals(message, e.message)
        server.verifyUpdate(request, token)
    }

    @Test
    fun `update returns on 200`() {
        val response = UpdateResponse(Random.nextString(8))
        server.enqueue {
            setResponseCode(200)
            setBody(json.encodeToString(Box(response)))
        }
        val token = Random.nextString(8)
        val request = Random.nextUpdateRequest()
        assertEquals(response, step.update(request, token).execute())
        server.verifyUpdate(request, token)
    }

    @Test
    fun `dtmf throws IllegalStateException`() {
        server.enqueue { setResponseCode(500) }
        val request = DtmfRequest(Random.nextDigits(8))
        val token = Random.nextString(8)
        assertFailsWith<IllegalStateException> { step.dtmf(request, token).execute() }
        server.verifyDtmf(request, token)
    }

    @Test
    fun `dtmf throws NoSuchNodeException`() {
        server.enqueue { setResponseCode(404) }
        val request = DtmfRequest(Random.nextDigits(8))
        val token = Random.nextString(8)
        assertFailsWith<NoSuchNodeException> { step.dtmf(request, token).execute() }
        server.verifyDtmf(request, token)
    }

    @Test
    fun `dtmf throws NoSuchConferenceException`() {
        val message = "Neither conference nor gateway found"
        server.enqueue {
            setResponseCode(404)
            setBody(json.encodeToString(Box(message)))
        }
        val request = DtmfRequest(Random.nextDigits(8))
        val token = Random.nextString(8)
        assertFailsWith<NoSuchConferenceException> { step.dtmf(request, token).execute() }
        server.verifyDtmf(request, token)
    }

    @Test
    fun `dtmf throws InvalidTokenException`() {
        val message = "Invalid token"
        server.enqueue {
            setResponseCode(403)
            setBody(json.encodeToString(Box(message)))
        }
        val request = DtmfRequest(Random.nextDigits(8))
        val token = Random.nextString(8)
        assertFailsWith<InvalidTokenException> { step.dtmf(request, token).execute() }
        server.verifyDtmf(request, token)
    }

    @Test
    fun `dtmf returns`() {
        val response = Random.nextBoolean()
        server.enqueue {
            setResponseCode(200)
            setBody(json.encodeToString(Box(response)))
        }
        val request = DtmfRequest(Random.nextDigits(8))
        val token = Random.nextString(8)
        assertEquals(response, step.dtmf(request, token).execute())
        server.verifyDtmf(request, token)
    }

    private fun MockWebServer.verifyNewCandidate(
        request: NewCandidateRequest,
        token: String,
    ) = takeRequest {
        assertRequestUrl(node) {
            addPathSegments("api/client/v2")
            addPathSegment("conferences")
            addPathSegment(conferenceAlias)
            addPathSegment("participants")
            addPathSegment(participantId)
            addPathSegment("calls")
            addPathSegment(callId)
            addPathSegment("new_candidate")
        }
        assertToken(token)
        assertPost(json, request)
    }

    private fun MockWebServer.verifyAck(token: String) = takeRequest {
        assertRequestUrl(node) {
            addPathSegments("api/client/v2")
            addPathSegment("conferences")
            addPathSegment(conferenceAlias)
            addPathSegment("participants")
            addPathSegment(participantId)
            addPathSegment("calls")
            addPathSegment(callId)
            addPathSegment("ack")
        }
        assertToken(token)
        assertPostEmptyBody()
    }

    private fun MockWebServer.verifyUpdate(request: UpdateRequest, token: String) = takeRequest {
        assertRequestUrl(node) {
            addPathSegments("api/client/v2")
            addPathSegment("conferences")
            addPathSegment(conferenceAlias)
            addPathSegment("participants")
            addPathSegment(participantId)
            addPathSegment("calls")
            addPathSegment(callId)
            addPathSegment("update")
        }
        assertToken(token)
        assertPost(json, request)
    }

    private fun MockWebServer.verifyDtmf(request: DtmfRequest, token: String) = takeRequest {
        assertRequestUrl(node) {
            addPathSegments("api/client/v2")
            addPathSegment("conferences")
            addPathSegment(conferenceAlias)
            addPathSegment("participants")
            addPathSegment(participantId)
            addPathSegments("calls")
            addPathSegment(callId)
            addPathSegment("dtmf")
        }
        assertToken(token)
        assertPost(json, request)
    }

    private fun Random.nextNewCandidateRequest() = NewCandidateRequest(
        candidate = nextString(8),
        mid = nextString(8),
        ufrag = nextString(8),
        pwd = nextString(8)
    )

    private fun Random.nextUpdateRequest() = UpdateRequest(
        sdp = nextString(8),
        fecc = nextBoolean()
    )
}
