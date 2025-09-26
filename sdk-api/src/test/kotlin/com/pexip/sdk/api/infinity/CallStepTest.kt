/*
 * Copyright 2022-2025 Pexip AS
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

import assertk.Table2
import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.hasMessage
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.tableOf
import com.pexip.sdk.api.infinity.internal.addPathSegment
import com.pexip.sdk.infinity.BreakoutId
import com.pexip.sdk.infinity.CallId
import com.pexip.sdk.infinity.ParticipantId
import com.pexip.sdk.infinity.test.nextBreakoutId
import com.pexip.sdk.infinity.test.nextCallId
import com.pexip.sdk.infinity.test.nextParticipantId
import com.pexip.sdk.infinity.test.nextString
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import mockwebserver3.MockWebServer
import okhttp3.HttpUrl
import org.junit.Rule
import kotlin.properties.Delegates
import kotlin.random.Random
import kotlin.test.BeforeTest
import kotlin.test.Test

internal class CallStepTest {

    @get:Rule
    val rule = SecureMockWebServerRule()

    private val server get() = rule.server
    private val client get() = rule.client

    private lateinit var node: HttpUrl
    private lateinit var conferenceAlias: String
    private lateinit var json: Json
    private lateinit var token: Token
    private lateinit var steps: Table2<BreakoutId?, InfinityService.CallStep>

    private var breakoutId: BreakoutId by Delegates.notNull()
    private var participantId: ParticipantId by Delegates.notNull()
    private var callId: CallId by Delegates.notNull()

    @BeforeTest
    fun setUp() {
        node = server.url("/")
        conferenceAlias = Random.nextString()
        breakoutId = Random.nextBreakoutId()
        participantId = Random.nextParticipantId()
        callId = Random.nextCallId()
        json = InfinityService.Json
        token = Random.nextFakeToken()
        val step = InfinityService.create(client, json)
            .newRequest(node)
            .conference(conferenceAlias)
        steps = tableOf("breakoutId", "step")
            .row<BreakoutId?, InfinityService.CallStep>(
                val1 = null,
                val2 = step
                    .participant(participantId)
                    .call(callId),
            )
            .row(
                val1 = breakoutId,
                val2 = step
                    .breakout(breakoutId)
                    .participant(participantId)
                    .call(callId),
            )
    }

    @Test
    fun `callId returns the correct value`() = runTestForAll { _, step ->
        assertThat(step::callId).isEqualTo(callId)
    }

    @Test
    fun `newCandidate throws IllegalStateException`() = runTestForAll { breakoutId, step ->
        server.enqueue { code(500) }
        val request = Random.nextNewCandidateRequest()
        assertFailure { step.newCandidate(request, token).await() }
            .isInstanceOf<IllegalStateException>()
        server.verifyNewCandidate(request, token, breakoutId)
    }

    @Test
    fun `newCandidate throws NoSuchNodeException`() = runTestForAll { breakoutId, step ->
        server.enqueue { code(404) }
        val request = Random.nextNewCandidateRequest()
        assertFailure { step.newCandidate(request, token).await() }
            .isInstanceOf<NoSuchNodeException>()
        server.verifyNewCandidate(request, token, breakoutId)
    }

    @Test
    fun `newCandidate throws NoSuchConferenceException`() = runTestForAll { breakoutId, step ->
        val message = "Neither conference nor gateway found"
        server.enqueue {
            code(404)
            body(json.encodeToString(Box(message)))
        }
        val request = Random.nextNewCandidateRequest()
        assertFailure { step.newCandidate(request, token).await() }
            .isInstanceOf<NoSuchConferenceException>()
            .hasMessage(message)
        server.verifyNewCandidate(request, token, breakoutId)
    }

    @Test
    fun `newCandidate throws InvalidTokenException`() = runTestForAll { breakoutId, step ->
        val message = "Invalid token"
        server.enqueue {
            code(403)
            body(json.encodeToString(Box(message)))
        }
        val request = Random.nextNewCandidateRequest()
        assertFailure { step.newCandidate(request, token).await() }
            .isInstanceOf<InvalidTokenException>()
            .hasMessage(message)
        server.verifyNewCandidate(request, token, breakoutId)
    }

    @Test
    fun `newCandidate returns on 200`() = runTestForAll { breakoutId, step ->
        server.enqueue { code(200) }
        val request = Random.nextNewCandidateRequest()
        step.newCandidate(request, token).await()
        server.verifyNewCandidate(request, token, breakoutId)
    }

    @Test
    fun `ack throws IllegalStateException`() = runTestForAll { breakoutId, step ->
        server.enqueue { code(500) }
        val request = Random.maybe { nextAckRequest() }
        val call = when (request) {
            null -> step.ack(token)
            else -> step.ack(request, token)
        }
        assertFailure { call.await() }.isInstanceOf<IllegalStateException>()
        server.verifyAck(request, token, breakoutId)
    }

    @Test
    fun `ack throws NoSuchNodeException`() = runTestForAll { breakoutId, step ->
        server.enqueue { code(404) }
        val request = Random.maybe { nextAckRequest() }
        val call = when (request) {
            null -> step.ack(token)
            else -> step.ack(request, token)
        }
        assertFailure { call.await() }.isInstanceOf<NoSuchNodeException>()
        server.verifyAck(request, token, breakoutId)
    }

    @Test
    fun `ack throws NoSuchConferenceException`() = runTestForAll { breakoutId, step ->
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
        assertFailure { call.await() }
            .isInstanceOf<NoSuchConferenceException>()
            .hasMessage(message)
        server.verifyAck(request, token, breakoutId)
    }

    @Test
    fun `ack throws InvalidTokenException`() = runTestForAll { breakoutId, step ->
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
        assertFailure { call.await() }
            .isInstanceOf<InvalidTokenException>()
            .hasMessage(message)
        server.verifyAck(request, token, breakoutId)
    }

    @Test
    fun `ack returns on 200`() = runTestForAll { breakoutId, step ->
        server.enqueue { code(200) }
        val request = Random.maybe { nextAckRequest() }
        val call = when (request) {
            null -> step.ack(token)
            else -> step.ack(request, token)
        }
        call.await()
        server.verifyAck(request, token, breakoutId)
    }

    @Test
    fun `update throws IllegalStateException`() = runTestForAll { breakoutId, step ->
        server.enqueue { code(500) }
        val request = Random.nextUpdateRequest()
        assertFailure { step.update(request, token).await() }.isInstanceOf<IllegalStateException>()
        server.verifyUpdate(request, token, breakoutId)
    }

    @Test
    fun `update throws NoSuchNodeException`() = runTestForAll { breakoutId, step ->
        server.enqueue { code(404) }
        val request = Random.nextUpdateRequest()
        assertFailure { step.update(request, token).await() }.isInstanceOf<NoSuchNodeException>()
        server.verifyUpdate(request, token, breakoutId)
    }

    @Test
    fun `update throws NoSuchConferenceException`() = runTestForAll { breakoutId, step ->
        val message = "Neither conference nor gateway found"
        server.enqueue {
            code(404)
            body(json.encodeToString(Box(message)))
        }
        val request = Random.nextUpdateRequest()
        assertFailure { step.update(request, token).await() }
            .isInstanceOf<NoSuchConferenceException>()
            .hasMessage(message)
        server.verifyUpdate(request, token, breakoutId)
    }

    @Test
    fun `update throws InvalidTokenException`() = runTestForAll { breakoutId, step ->
        val message = "Invalid token"
        server.enqueue {
            code(403)
            body(json.encodeToString(Box(message)))
        }
        val request = Random.nextUpdateRequest()
        assertFailure { step.update(request, token).await() }
            .isInstanceOf<InvalidTokenException>()
            .hasMessage(message)
        server.verifyUpdate(request, token, breakoutId)
    }

    @Test
    fun `update returns on 200`() = runTestForAll { breakoutId, step ->
        val response = UpdateResponse(Random.nextString())
        server.enqueue {
            code(200)
            body(json.encodeToString(Box(response)))
        }
        val request = Random.nextUpdateRequest()
        assertThat(step.update(request, token).await()).isEqualTo(response)
        server.verifyUpdate(request, token, breakoutId)
    }

    @Test
    fun `dtmf throws IllegalStateException`() = runTestForAll { breakoutId, step ->
        server.enqueue { code(500) }
        val request = DtmfRequest(Random.nextDigits(8))
        assertFailure { step.dtmf(request, token).await() }.isInstanceOf<IllegalStateException>()
        server.verifyDtmf(request, token, breakoutId)
    }

    @Test
    fun `dtmf throws NoSuchNodeException`() = runTestForAll { breakoutId, step ->
        server.enqueue { code(404) }
        val request = DtmfRequest(Random.nextDigits(8))
        assertFailure { step.dtmf(request, token).await() }.isInstanceOf<NoSuchNodeException>()
        server.verifyDtmf(request, token, breakoutId)
    }

    @Test
    fun `dtmf throws NoSuchConferenceException`() = runTestForAll { breakoutId, step ->
        val message = "Neither conference nor gateway found"
        server.enqueue {
            code(404)
            body(json.encodeToString(Box(message)))
        }
        val request = DtmfRequest(Random.nextDigits(8))
        assertFailure { step.dtmf(request, token).await() }
            .isInstanceOf<NoSuchConferenceException>()
            .hasMessage(message)
        server.verifyDtmf(request, token, breakoutId)
    }

    @Test
    fun `dtmf throws InvalidTokenException`() = runTestForAll { breakoutId, step ->
        val message = "Invalid token"
        server.enqueue {
            code(403)
            body(json.encodeToString(Box(message)))
        }
        val request = DtmfRequest(Random.nextDigits(8))
        assertFailure { step.dtmf(request, token).await() }
            .isInstanceOf<InvalidTokenException>()
            .hasMessage(message)
        server.verifyDtmf(request, token, breakoutId)
    }

    @Test
    fun `dtmf returns`() = runTestForAll { breakoutId, step ->
        val response = Random.nextBoolean()
        server.enqueue {
            code(200)
            body(json.encodeToString(Box(response)))
        }
        val request = DtmfRequest(Random.nextDigits(8))
        assertThat(step.dtmf(request, token).await()).isEqualTo(response)
        server.verifyDtmf(request, token, breakoutId)
    }

    @Test
    fun `disconnect throws IllegalStateException`() = runTestForAll { breakoutId, step ->
        server.enqueue { code(500) }
        assertFailure { step.disconnect(token).await() }.isInstanceOf<IllegalStateException>()
        server.verifyDisconnect(token, breakoutId)
    }

    @Test
    fun `disconnect throws NoSuchNodeException`() = runTestForAll { breakoutId, step ->
        server.enqueue { code(404) }
        assertFailure { step.disconnect(token).await() }.isInstanceOf<NoSuchNodeException>()
        server.verifyDisconnect(token, breakoutId)
    }

    @Test
    fun `disconnect throws NoSuchConferenceException`() = runTestForAll { breakoutId, step ->
        val message = "Neither conference nor gateway found"
        server.enqueue {
            code(404)
            body(json.encodeToString(Box(message)))
        }
        assertFailure { step.disconnect(token).await() }
            .isInstanceOf<NoSuchConferenceException>()
            .hasMessage(message)
        server.verifyDisconnect(token, breakoutId)
    }

    @Test
    fun `disconnect throws InvalidTokenException`() = runTestForAll { breakoutId, step ->
        val message = "Invalid token"
        server.enqueue {
            code(403)
            body(json.encodeToString(Box(message)))
        }
        assertFailure { step.disconnect(token).await() }
            .isInstanceOf<InvalidTokenException>()
            .hasMessage(message)
        server.verifyDisconnect(token, breakoutId)
    }

    @Test
    fun `disconnect returns`() = runTestForAll { breakoutId, step ->
        val response = Random.nextBoolean()
        server.enqueue {
            code(200)
            body(json.encodeToString(Box(response)))
        }
        assertThat(step.disconnect(token).await()).isEqualTo(response)
        server.verifyDisconnect(token, breakoutId)
    }

    private fun runTestForAll(
        testBody: suspend TestScope.(BreakoutId?, InfinityService.CallStep) -> Unit,
    ) = steps.forAll { breakoutId, step -> runTest { testBody(breakoutId, step) } }

    private fun MockWebServer.verifyNewCandidate(
        request: NewCandidateRequest,
        token: Token,
        breakoutId: BreakoutId?,
    ) = takeRequest {
        assertRequestUrl(node) {
            addPathSegments("api/client/v2")
            addPathSegment("conferences")
            addPathSegment(conferenceAlias)
            if (breakoutId != null) {
                addPathSegment("breakouts")
                addPathSegment(breakoutId)
            }
            addPathSegment("participants")
            addPathSegment(participantId)
            addPathSegment("calls")
            addPathSegment(callId)
            addPathSegment("new_candidate")
        }
        assertToken(token)
        assertPost(json, request)
    }

    private fun MockWebServer.verifyAck(
        request: AckRequest?,
        token: Token,
        breakoutId: BreakoutId?,
    ) = takeRequest {
        assertRequestUrl(node) {
            addPathSegments("api/client/v2")
            addPathSegment("conferences")
            addPathSegment(conferenceAlias)
            if (breakoutId != null) {
                addPathSegment("breakouts")
                addPathSegment(breakoutId)
            }
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

    private fun MockWebServer.verifyUpdate(
        request: UpdateRequest,
        token: Token,
        breakoutId: BreakoutId?,
    ) = takeRequest {
        assertRequestUrl(node) {
            addPathSegments("api/client/v2")
            addPathSegment("conferences")
            addPathSegment(conferenceAlias)
            if (breakoutId != null) {
                addPathSegment("breakouts")
                addPathSegment(breakoutId)
            }
            addPathSegment("participants")
            addPathSegment(participantId)
            addPathSegment("calls")
            addPathSegment(callId)
            addPathSegment("update")
        }
        assertToken(token)
        assertPost(json, request)
    }

    private fun MockWebServer.verifyDtmf(
        request: DtmfRequest,
        token: Token,
        breakoutId: BreakoutId?,
    ) = takeRequest {
        assertRequestUrl(node) {
            addPathSegments("api/client/v2")
            addPathSegment("conferences")
            addPathSegment(conferenceAlias)
            if (breakoutId != null) {
                addPathSegment("breakouts")
                addPathSegment(breakoutId)
            }
            addPathSegment("participants")
            addPathSegment(participantId)
            addPathSegments("calls")
            addPathSegment(callId)
            addPathSegment("dtmf")
        }
        assertToken(token)
        assertPost(json, request)
    }

    private fun MockWebServer.verifyDisconnect(token: Token, breakoutId: BreakoutId?) =
        takeRequest {
            assertRequestUrl(node) {
                addPathSegments("api/client/v2")
                addPathSegment("conferences")
                addPathSegment(conferenceAlias)
                if (breakoutId != null) {
                    addPathSegment("breakouts")
                    addPathSegment(breakoutId)
                }
                addPathSegment("participants")
                addPathSegment(participantId)
                addPathSegments("calls")
                addPathSegment(callId)
                addPathSegment("disconnect")
            }
            assertToken(token)
            assertPostEmptyBody()
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
