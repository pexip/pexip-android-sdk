/*
 * Copyright 2022-2024 Pexip AS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.pexip.sdk.api.infinity

import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.hasMessage
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import com.pexip.sdk.api.coroutines.await
import com.pexip.sdk.api.infinity.internal.addPathSegment
import com.pexip.sdk.infinity.CallId
import com.pexip.sdk.infinity.ParticipantId
import com.pexip.sdk.infinity.test.nextCallId
import com.pexip.sdk.infinity.test.nextParticipantId
import com.pexip.sdk.infinity.test.nextString
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import mockwebserver3.MockWebServer
import mockwebserver3.junit4.MockWebServerRule
import okhttp3.OkHttpClient
import org.junit.Rule
import java.net.URL
import kotlin.properties.Delegates
import kotlin.random.Random
import kotlin.test.BeforeTest
import kotlin.test.Test

internal class CallStepTest {

    @get:Rule
    val rule = MockWebServerRule()

    private val server = rule.server

    private lateinit var node: URL
    private lateinit var conferenceAlias: String
    private lateinit var json: Json
    private lateinit var token: Token
    private lateinit var step: InfinityService.CallStep

    private var participantId: ParticipantId by Delegates.notNull()
    private var callId: CallId by Delegates.notNull()

    @BeforeTest
    fun setUp() {
        node = server.url("/").toUrl()
        conferenceAlias = Random.nextString()
        participantId = Random.nextParticipantId()
        callId = Random.nextCallId()
        json = Json { ignoreUnknownKeys = true }
        val service = InfinityService.create(OkHttpClient(), json)
        token = Random.nextFakeToken()
        step = service.newRequest(node)
            .conference(conferenceAlias)
            .participant(participantId)
            .call(callId)
    }

    @Test
    fun `newCandidate throws IllegalStateException`() = runTest {
        server.enqueue { code(500) }
        val request = Random.nextNewCandidateRequest()
        assertFailure { step.newCandidate(request, token).await() }
            .isInstanceOf<IllegalStateException>()
        server.verifyNewCandidate(request, token)
    }

    @Test
    fun `newCandidate throws NoSuchNodeException`() = runTest {
        server.enqueue { code(404) }
        val request = Random.nextNewCandidateRequest()
        assertFailure { step.newCandidate(request, token).await() }
            .isInstanceOf<NoSuchNodeException>()
        server.verifyNewCandidate(request, token)
    }

    @Test
    fun `newCandidate throws NoSuchConferenceException`() = runTest {
        val message = "Neither conference nor gateway found"
        server.enqueue {
            code(404)
            body(json.encodeToString(Box(message)))
        }
        val request = Random.nextNewCandidateRequest()
        assertFailure { step.newCandidate(request, token).await() }
            .isInstanceOf<NoSuchConferenceException>()
        server.verifyNewCandidate(request, token)
    }

    @Test
    fun `newCandidate throws InvalidTokenException`() = runTest {
        val message = "Invalid token"
        server.enqueue {
            code(403)
            body(json.encodeToString(Box(message)))
        }
        val request = Random.nextNewCandidateRequest()
        assertFailure { step.newCandidate(request, token).await() }
            .isInstanceOf<InvalidTokenException>()
        server.verifyNewCandidate(request, token)
    }

    @Test
    fun `newCandidate returns on 200`() = runTest {
        server.enqueue { code(200) }
        val request = Random.nextNewCandidateRequest()
        step.newCandidate(request, token).await()
        server.verifyNewCandidate(request, token)
    }

    @Test
    fun `ack throws IllegalStateException`() = runTest {
        server.enqueue { code(500) }
        val request = Random.maybe { nextAckRequest() }
        val call = when (request) {
            null -> step.ack(token)
            else -> step.ack(request, token)
        }
        assertFailure { call.await() }.isInstanceOf<IllegalStateException>()
        server.verifyAck(request, token)
    }

    @Test
    fun `ack throws NoSuchNodeException`() = runTest {
        server.enqueue { code(404) }
        val request = Random.maybe { nextAckRequest() }
        val call = when (request) {
            null -> step.ack(token)
            else -> step.ack(request, token)
        }
        assertFailure { call.await() }.isInstanceOf<NoSuchNodeException>()
        server.verifyAck(request, token)
    }

    @Test
    fun `ack throws NoSuchConferenceException`() = runTest {
        val message = "Neither conference nor gateway found"
        server.enqueue {
            code(404)
            body(json.encodeToString(Box(message)))
        }
        val request = Random.maybe { nextAckRequest() }
        val call = when (request) {
            null -> step.ack(token)
            else -> step.ack(request, token)
        }
        assertFailure { call.await() }.isInstanceOf<NoSuchConferenceException>()
        server.verifyAck(request, token)
    }

    @Test
    fun `ack throws InvalidTokenException`() = runTest {
        val message = "Invalid token"
        server.enqueue {
            code(403)
            body(json.encodeToString(Box(message)))
        }
        val request = Random.maybe { nextAckRequest() }
        val call = when (request) {
            null -> step.ack(token)
            else -> step.ack(request, token)
        }
        assertFailure { call.await() }.isInstanceOf<InvalidTokenException>()
        server.verifyAck(request, token)
    }

    @Test
    fun `ack returns on 200`() = runTest {
        server.enqueue { code(200) }
        val request = Random.maybe { nextAckRequest() }
        val call = when (request) {
            null -> step.ack(token)
            else -> step.ack(request, token)
        }
        call.await()
        server.verifyAck(request, token)
    }

    @Test
    fun `update throws IllegalStateException`() = runTest {
        server.enqueue { code(500) }
        val request = Random.nextUpdateRequest()
        assertFailure { step.update(request, token).await() }.isInstanceOf<IllegalStateException>()
        server.verifyUpdate(request, token)
    }

    @Test
    fun `update throws NoSuchNodeException`() = runTest {
        server.enqueue { code(404) }
        val request = Random.nextUpdateRequest()
        assertFailure { step.update(request, token).await() }.isInstanceOf<NoSuchNodeException>()
        server.verifyUpdate(request, token)
    }

    @Test
    fun `update throws NoSuchConferenceException`() = runTest {
        val message = "Neither conference nor gateway found"
        server.enqueue {
            code(404)
            body(json.encodeToString(Box(message)))
        }
        val request = Random.nextUpdateRequest()
        assertFailure { step.update(request, token).await() }
            .isInstanceOf<NoSuchConferenceException>()
            .hasMessage(message)
        server.verifyUpdate(request, token)
    }

    @Test
    fun `update throws InvalidTokenException`() = runTest {
        val message = "Invalid token"
        server.enqueue {
            code(403)
            body(json.encodeToString(Box(message)))
        }
        val request = Random.nextUpdateRequest()
        assertFailure { step.update(request, token).await() }
            .isInstanceOf<InvalidTokenException>()
            .hasMessage(message)
        server.verifyUpdate(request, token)
    }

    @Test
    fun `update returns on 200`() = runTest {
        val response = UpdateResponse(Random.nextString())
        server.enqueue {
            code(200)
            body(json.encodeToString(Box(response)))
        }
        val request = Random.nextUpdateRequest()
        assertThat(step.update(request, token).await()).isEqualTo(response)
        server.verifyUpdate(request, token)
    }

    @Test
    fun `dtmf throws IllegalStateException`() = runTest {
        server.enqueue { code(500) }
        val request = DtmfRequest(Random.nextDigits(8))
        assertFailure { step.dtmf(request, token).await() }.isInstanceOf<IllegalStateException>()
        server.verifyDtmf(request, token)
    }

    @Test
    fun `dtmf throws NoSuchNodeException`() = runTest {
        server.enqueue { code(404) }
        val request = DtmfRequest(Random.nextDigits(8))
        assertFailure { step.dtmf(request, token).await() }.isInstanceOf<NoSuchNodeException>()
        server.verifyDtmf(request, token)
    }

    @Test
    fun `dtmf throws NoSuchConferenceException`() = runTest {
        val message = "Neither conference nor gateway found"
        server.enqueue {
            code(404)
            body(json.encodeToString(Box(message)))
        }
        val request = DtmfRequest(Random.nextDigits(8))
        assertFailure { step.dtmf(request, token).await() }
            .isInstanceOf<NoSuchConferenceException>()
        server.verifyDtmf(request, token)
    }

    @Test
    fun `dtmf throws InvalidTokenException`() = runTest {
        val message = "Invalid token"
        server.enqueue {
            code(403)
            body(json.encodeToString(Box(message)))
        }
        val request = DtmfRequest(Random.nextDigits(8))
        assertFailure { step.dtmf(request, token).await() }.isInstanceOf<InvalidTokenException>()
        server.verifyDtmf(request, token)
    }

    @Test
    fun `dtmf returns`() = runTest {
        val response = Random.nextBoolean()
        server.enqueue {
            code(200)
            body(json.encodeToString(Box(response)))
        }
        val request = DtmfRequest(Random.nextDigits(8))
        assertThat(step.dtmf(request, token).await()).isEqualTo(response)
        server.verifyDtmf(request, token)
    }

    private fun MockWebServer.verifyNewCandidate(
        request: NewCandidateRequest,
        token: Token,
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

    private fun MockWebServer.verifyAck(request: AckRequest?, token: Token) = takeRequest {
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
        when (request) {
            null -> assertPostEmptyBody()
            else -> assertPost(json, request)
        }
    }

    private fun MockWebServer.verifyUpdate(request: UpdateRequest, token: Token) = takeRequest {
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

    private fun MockWebServer.verifyDtmf(request: DtmfRequest, token: Token) = takeRequest {
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
        candidate = nextString(),
        mid = nextString(),
        ufrag = nextString(),
        pwd = nextString(),
    )

    private fun Random.nextUpdateRequest() = UpdateRequest(
        sdp = nextString(),
        fecc = nextBoolean(),
    )

    private fun Random.nextAckRequest(): AckRequest {
        val offerIgnored = nextBoolean()
        return AckRequest(
            sdp = if (offerIgnored) "" else nextString(),
            offerIgnored = offerIgnored,
        )
    }

    private inline fun <T : Any> Random.maybe(block: Random.() -> T) = when (nextBoolean()) {
        true -> block()
        else -> null
    }
}
