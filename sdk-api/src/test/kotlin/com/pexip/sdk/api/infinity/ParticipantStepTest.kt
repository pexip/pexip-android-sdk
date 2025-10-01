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

internal class ParticipantStepTest {

    @get:Rule
    val rule = SecureMockWebServerRule()

    private val server get() = rule.server
    private val client get() = rule.client

    private lateinit var node: HttpUrl
    private lateinit var conferenceAlias: String
    private lateinit var json: Json
    private lateinit var token: Token
    private lateinit var steps: Table2<BreakoutId?, InfinityService.ParticipantStep>

    private var breakoutId: BreakoutId by Delegates.notNull()
    private var participantId: ParticipantId by Delegates.notNull()

    @BeforeTest
    fun setUp() {
        node = server.url("/")
        conferenceAlias = Random.nextString()
        breakoutId = Random.nextBreakoutId()
        participantId = Random.nextParticipantId()
        json = InfinityService.Json
        token = Random.nextFakeToken()
        val step = InfinityService.create(client, json)
            .newRequest(node)
            .conference(conferenceAlias)
        steps = tableOf("breakoutId", "step")
            .row<BreakoutId?, InfinityService.ParticipantStep>(
                val1 = null,
                val2 = step.participant(participantId),
            )
            .row(
                val1 = breakoutId,
                val2 = step
                    .breakout(breakoutId)
                    .participant(participantId),
            )
    }

    @Test
    fun `participantId returns the correct value`() = runTestForAll { _, step ->
        assertThat(step::participantId).isEqualTo(participantId)
    }

    @Test
    fun `calls throws IllegalStateException`() = runTestForAll { breakoutId, step ->
        server.enqueue { code(500) }
        val request = Random.nextCallsRequest()
        assertFailure { step.calls(request, token).await() }.isInstanceOf<IllegalStateException>()
        server.verifyCalls(request, token, breakoutId)
    }

    @Test
    fun `calls throws NoSuchNodeException`() = runTestForAll { breakoutId, step ->
        server.enqueue { code(404) }
        val request = Random.nextCallsRequest()
        assertFailure { step.calls(request, token).await() }.isInstanceOf<NoSuchNodeException>()
        server.verifyCalls(request, token, breakoutId)
    }

    @Test
    fun `calls throws NoSuchConferenceException`() = runTestForAll { breakoutId, step ->
        val message = "Neither conference nor gateway found"
        server.enqueue {
            code(404)
            body(json.encodeToString(Box(message)))
        }
        val request = Random.nextCallsRequest()
        assertFailure { step.calls(request, token).await() }
            .isInstanceOf<NoSuchConferenceException>()
            .hasMessage(message)
        server.verifyCalls(request, token, breakoutId)
    }

    @Test
    fun `calls throws InvalidTokenException`() = runTestForAll { breakoutId, step ->
        val message = "Invalid token"
        server.enqueue {
            code(403)
            body(json.encodeToString(Box(message)))
        }
        val request = Random.nextCallsRequest()
        assertFailure { step.calls(request, token).await() }
            .isInstanceOf<InvalidTokenException>()
            .hasMessage(message)
        server.verifyCalls(request, token, breakoutId)
    }

    @Test
    fun `calls returns CallsResponse`() = runTestForAll { breakoutId, step ->
        val response = CallsResponse(
            callId = Random.nextCallId(),
            sdp = Random.nextString(),
        )
        server.enqueue {
            code(200)
            body(json.encodeToString(Box(response)))
        }
        val request = Random.nextCallsRequest()
        assertThat(step.calls(request, token).await(), "response").isEqualTo(response)
        server.verifyCalls(request, token, breakoutId)
    }

    @Test
    fun `dtmf throws IllegalStateException`() = runTestForAll { breakoutId, step ->
        server.enqueue { code(500) }
        val request = DtmfRequest(Random.nextDigits())
        assertFailure { step.dtmf(request, token).await() }.isInstanceOf<IllegalStateException>()
        server.verifyDtmf(request, token, breakoutId)
    }

    @Test
    fun `dtmf throws NoSuchNodeException`() = runTestForAll { breakoutId, step ->
        server.enqueue { code(404) }
        val request = DtmfRequest(Random.nextDigits())
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
        val request = DtmfRequest(Random.nextDigits())
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
        val request = DtmfRequest(Random.nextDigits())
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
        val request = DtmfRequest(Random.nextDigits())
        assertThat(step.dtmf(request, token).await(), "response").isEqualTo(response)
        server.verifyDtmf(request, token, breakoutId)
    }

    @Test
    fun `mute throws IllegalStateException`() = runTestForAll { breakoutId, step ->
        server.enqueue { code(500) }
        assertFailure { step.mute(token).await() }.isInstanceOf<IllegalStateException>()
        server.verifyMute(token, breakoutId)
    }

    @Test
    fun `mute throws NoSuchNodeException`() = runTestForAll { breakoutId, step ->
        server.enqueue { code(404) }
        assertFailure { step.mute(token).await() }.isInstanceOf<NoSuchNodeException>()
        server.verifyMute(token, breakoutId)
    }

    @Test
    fun `mute throws NoSuchConferenceException`() = runTestForAll { breakoutId, step ->
        val message = "Neither conference nor gateway found"
        server.enqueue {
            code(404)
            body(json.encodeToString(Box(message)))
        }
        assertFailure { step.mute(token).await() }
            .isInstanceOf<NoSuchConferenceException>()
            .hasMessage(message)
        server.verifyMute(token, breakoutId)
    }

    @Test
    fun `mute throws InvalidTokenException`() = runTestForAll { breakoutId, step ->
        val message = "Invalid token"
        server.enqueue {
            code(403)
            body(json.encodeToString(Box(message)))
        }
        assertFailure { step.mute(token).await() }
            .isInstanceOf<InvalidTokenException>()
            .hasMessage(message)
        server.verifyMute(token, breakoutId)
    }

    @Test
    fun `mute returns`() = runTestForAll { breakoutId, step ->
        server.enqueue { code(200) }
        step.mute(token).await()
        server.verifyMute(token, breakoutId)
    }

    @Test
    fun `unmute throws IllegalStateException`() = runTestForAll { breakoutId, step ->
        server.enqueue { code(500) }
        assertFailure { step.unmute(token).await() }.isInstanceOf<IllegalStateException>()
        server.verifyUnmute(token, breakoutId)
    }

    @Test
    fun `unmute throws NoSuchNodeException`() = runTestForAll { breakoutId, step ->
        server.enqueue { code(404) }
        assertFailure { step.unmute(token).await() }.isInstanceOf<NoSuchNodeException>()
        server.verifyUnmute(token, breakoutId)
    }

    @Test
    fun `unmute throws NoSuchConferenceException`() = runTestForAll { breakoutId, step ->
        val message = "Neither conference nor gateway found"
        server.enqueue {
            code(404)
            body(json.encodeToString(Box(message)))
        }
        assertFailure { step.unmute(token).await() }
            .isInstanceOf<NoSuchConferenceException>()
            .hasMessage(message)
        server.verifyUnmute(token, breakoutId)
    }

    @Test
    fun `unmute throws InvalidTokenException`() = runTestForAll { breakoutId, step ->
        val message = "Invalid token"
        server.enqueue {
            code(403)
            body(json.encodeToString(Box(message)))
        }
        assertFailure { step.unmute(token).await() }
            .isInstanceOf<InvalidTokenException>()
            .hasMessage(message)
        server.verifyUnmute(token, breakoutId)
    }

    @Test
    fun `unmute returns`() = runTestForAll { breakoutId, step ->
        server.enqueue { code(200) }
        step.unmute(token).await()
        server.verifyUnmute(token, breakoutId)
    }

    @Test
    fun `clientMute throws IllegalStateException`() = runTestForAll { breakoutId, step ->
        server.enqueue { code(500) }
        assertFailure { step.clientMute(token).await() }.isInstanceOf<IllegalStateException>()
        server.verifyClientMute(token, breakoutId)
    }

    @Test
    fun `clientMute throws NoSuchNodeException`() = runTestForAll { breakoutId, step ->
        server.enqueue { code(404) }
        assertFailure { step.clientMute(token).await() }.isInstanceOf<NoSuchNodeException>()
        server.verifyClientMute(token, breakoutId)
    }

    @Test
    fun `clientMute throws NoSuchConferenceException`() = runTestForAll { breakoutId, step ->
        val message = "Neither conference nor gateway found"
        server.enqueue {
            code(404)
            body(json.encodeToString(Box(message)))
        }
        assertFailure { step.clientMute(token).await() }
            .isInstanceOf<NoSuchConferenceException>()
            .hasMessage(message)
        server.verifyClientMute(token, breakoutId)
    }

    @Test
    fun `clientMute throws InvalidTokenException`() = runTestForAll { breakoutId, step ->
        val message = "Invalid token"
        server.enqueue {
            code(403)
            body(json.encodeToString(Box(message)))
        }
        assertFailure { step.clientMute(token).await() }
            .isInstanceOf<InvalidTokenException>()
            .hasMessage(message)
        server.verifyClientMute(token, breakoutId)
    }

    @Test
    fun `clientMute returns`() = runTestForAll { breakoutId, step ->
        server.enqueue { code(200) }
        step.clientMute(token).await()
        server.verifyClientMute(token, breakoutId)
    }

    @Test
    fun `clientUnmute throws IllegalStateException`() = runTestForAll { breakoutId, step ->
        server.enqueue { code(500) }
        assertFailure { step.clientUnmute(token).await() }.isInstanceOf<IllegalStateException>()
        server.verifyClientUnmute(token, breakoutId)
    }

    @Test
    fun `clientUnmute throws NoSuchNodeException`() = runTestForAll { breakoutId, step ->
        server.enqueue { code(404) }
        assertFailure { step.clientUnmute(token).await() }.isInstanceOf<NoSuchNodeException>()
        server.verifyClientUnmute(token, breakoutId)
    }

    @Test
    fun `clientUnmute throws NoSuchConferenceException`() = runTestForAll { breakoutId, step ->
        val message = "Neither conference nor gateway found"
        server.enqueue {
            code(404)
            body(json.encodeToString(Box(message)))
        }
        assertFailure { step.clientUnmute(token).await() }
            .isInstanceOf<NoSuchConferenceException>()
            .hasMessage(message)
        server.verifyClientUnmute(token, breakoutId)
    }

    @Test
    fun `clientUnmute throws InvalidTokenException`() = runTestForAll { breakoutId, step ->
        val message = "Invalid token"
        server.enqueue {
            code(403)
            body(json.encodeToString(Box(message)))
        }
        assertFailure { step.clientUnmute(token).await() }
            .isInstanceOf<InvalidTokenException>()
            .hasMessage(message)
        server.verifyClientUnmute(token, breakoutId)
    }

    @Test
    fun `clientUnmute returns`() = runTestForAll { breakoutId, step ->
        server.enqueue { code(200) }
        step.clientUnmute(token).await()
        server.verifyClientUnmute(token, breakoutId)
    }

    @Test
    fun `videoMuted throws IllegalStateException`() = runTestForAll { breakoutId, step ->
        server.enqueue { code(500) }
        assertFailure { step.videoMuted(token).await() }.isInstanceOf<IllegalStateException>()
        server.verifyVideoMuted(token, breakoutId)
    }

    @Test
    fun `videoMuted throws NoSuchNodeException`() = runTestForAll { breakoutId, step ->
        server.enqueue { code(404) }
        assertFailure { step.videoMuted(token).await() }.isInstanceOf<NoSuchNodeException>()
        server.verifyVideoMuted(token, breakoutId)
    }

    @Test
    fun `videoMuted throws NoSuchConferenceException`() = runTestForAll { breakoutId, step ->
        val message = "Neither conference nor gateway found"
        server.enqueue {
            code(404)
            body(json.encodeToString(Box(message)))
        }
        assertFailure { step.videoMuted(token).await() }
            .isInstanceOf<NoSuchConferenceException>()
            .hasMessage(message)
        server.verifyVideoMuted(token, breakoutId)
    }

    @Test
    fun `videoMuted throws InvalidTokenException`() = runTestForAll { breakoutId, step ->
        val message = "Invalid token"
        server.enqueue {
            code(403)
            body(json.encodeToString(Box(message)))
        }
        assertFailure { step.videoMuted(token).await() }
            .isInstanceOf<InvalidTokenException>()
            .hasMessage(message)
        server.verifyVideoMuted(token, breakoutId)
    }

    @Test
    fun `videoMuted returns`() = runTestForAll { breakoutId, step ->
        server.enqueue { code(200) }
        step.videoMuted(token).await()
        server.verifyVideoMuted(token, breakoutId)
    }

    @Test
    fun `videoUnmuted throws IllegalStateException`() = runTestForAll { breakoutId, step ->
        server.enqueue { code(500) }
        assertFailure { step.videoUnmuted(token).await() }.isInstanceOf<IllegalStateException>()
        server.verifyVideoUnMuted(token, breakoutId)
    }

    @Test
    fun `videoUnmuted throws NoSuchNodeException`() = runTestForAll { breakoutId, step ->
        server.enqueue { code(404) }
        assertFailure { step.videoUnmuted(token).await() }.isInstanceOf<NoSuchNodeException>()
        server.verifyVideoUnMuted(token, breakoutId)
    }

    @Test
    fun `videoUnmuted throws NoSuchConferenceException`() = runTestForAll { breakoutId, step ->
        val message = "Neither conference nor gateway found"
        server.enqueue {
            code(404)
            body(json.encodeToString(Box(message)))
        }
        assertFailure { step.videoUnmuted(token).await() }
            .isInstanceOf<NoSuchConferenceException>()
            .hasMessage(message)
        server.verifyVideoUnMuted(token, breakoutId)
    }

    @Test
    fun `videoUnmuted throws InvalidTokenException`() = runTestForAll { breakoutId, step ->
        val message = "Invalid token"
        server.enqueue {
            code(403)
            body(json.encodeToString(Box(message)))
        }
        assertFailure { step.videoUnmuted(token).await() }
            .isInstanceOf<InvalidTokenException>()
            .hasMessage(message)
        server.verifyVideoUnMuted(token, breakoutId)
    }

    @Test
    fun `videoUnmuted returns`() = runTestForAll { breakoutId, step ->
        server.enqueue { code(200) }
        step.videoUnmuted(token).await()
        server.verifyVideoUnMuted(token, breakoutId)
    }

    @Test
    fun `takeFloor throws IllegalStateException`() = runTestForAll { breakoutId, step ->
        server.enqueue { code(500) }
        assertFailure { step.takeFloor(token).await() }.isInstanceOf<IllegalStateException>()
        server.verifyTakeFloor(token, breakoutId)
    }

    @Test
    fun `takeFloor throws NoSuchNodeException`() = runTestForAll { breakoutId, step ->
        server.enqueue { code(404) }
        assertFailure { step.takeFloor(token).await() }.isInstanceOf<NoSuchNodeException>()
        server.verifyTakeFloor(token, breakoutId)
    }

    @Test
    fun `takeFloor throws NoSuchConferenceException`() = runTestForAll { breakoutId, step ->
        val message = "Neither conference nor gateway found"
        server.enqueue {
            code(404)
            body(json.encodeToString(Box(message)))
        }
        assertFailure { step.takeFloor(token).await() }
            .isInstanceOf<NoSuchConferenceException>()
            .hasMessage(message)
        server.verifyTakeFloor(token, breakoutId)
    }

    @Test
    fun `takeFloor throws InvalidTokenException`() = runTestForAll { breakoutId, step ->
        val message = "Invalid token"
        server.enqueue {
            code(403)
            body(json.encodeToString(Box(message)))
        }
        assertFailure { step.takeFloor(token).await() }
            .isInstanceOf<InvalidTokenException>()
            .hasMessage(message)
        server.verifyTakeFloor(token, breakoutId)
    }

    @Test
    fun `takeFloor returns`() = runTestForAll { breakoutId, step ->
        server.enqueue { code(200) }
        step.takeFloor(token).await()
        server.verifyTakeFloor(token, breakoutId)
    }

    @Test
    fun `releaseFloor throws IllegalStateException`() = runTestForAll { breakoutId, step ->
        server.enqueue { code(500) }
        assertFailure { step.releaseFloor(token).await() }.isInstanceOf<IllegalStateException>()
        server.verifyReleaseFloor(token, breakoutId)
    }

    @Test
    fun `releaseFloor throws NoSuchNodeException`() = runTestForAll { breakoutId, step ->
        server.enqueue { code(404) }
        assertFailure { step.releaseFloor(token).await() }.isInstanceOf<NoSuchNodeException>()
        server.verifyReleaseFloor(token, breakoutId)
    }

    @Test
    fun `releaseFloor throws NoSuchConferenceException`() = runTestForAll { breakoutId, step ->
        val message = "Neither conference nor gateway found"
        server.enqueue {
            code(404)
            body(json.encodeToString(Box(message)))
        }
        assertFailure { step.releaseFloor(token).await() }
            .isInstanceOf<NoSuchConferenceException>()
            .hasMessage(message)
        server.verifyReleaseFloor(token, breakoutId)
    }

    @Test
    fun `releaseFloor throws InvalidTokenException`() = runTestForAll { breakoutId, step ->
        val message = "Invalid token"
        server.enqueue {
            code(403)
            body(json.encodeToString(Box(message)))
        }
        assertFailure { step.releaseFloor(token).await() }
            .isInstanceOf<InvalidTokenException>()
            .hasMessage(message)
        server.verifyReleaseFloor(token, breakoutId)
    }

    @Test
    fun `releaseFloor returns`() = runTestForAll { breakoutId, step ->
        server.enqueue { code(200) }
        step.releaseFloor(token).await()
        server.verifyReleaseFloor(token, breakoutId)
    }

    @Test
    fun `buzz throws IllegalStateException`() = runTestForAll { breakoutId, step ->
        server.enqueue { code(500) }
        assertFailure { step.buzz(token).await() }.isInstanceOf<IllegalStateException>()
        server.verifyBuzz(token, breakoutId)
    }

    @Test
    fun `buzz throws NoSuchNodeException`() = runTestForAll { breakoutId, step ->
        server.enqueue { code(404) }
        assertFailure { step.buzz(token).await() }.isInstanceOf<NoSuchNodeException>()
        server.verifyBuzz(token, breakoutId)
    }

    @Test
    fun `buzz throws NoSuchConferenceException`() = runTestForAll { breakoutId, step ->
        val message = "Neither conference nor gateway found"
        server.enqueue {
            code(404)
            body(json.encodeToString(Box(message)))
        }
        assertFailure { step.buzz(token).await() }
            .isInstanceOf<NoSuchConferenceException>()
            .hasMessage(message)
        server.verifyBuzz(token, breakoutId)
    }

    @Test
    fun `buzz throws InvalidTokenException`() = runTestForAll { breakoutId, step ->
        val message = "Invalid token"
        server.enqueue {
            code(403)
            body(json.encodeToString(Box(message)))
        }
        assertFailure { step.buzz(token).await() }
            .isInstanceOf<InvalidTokenException>()
            .hasMessage(message)
        server.verifyBuzz(token, breakoutId)
    }

    @Test
    fun `buzz returns result on 200`() = runTestForAll { breakoutId, step ->
        val results = listOf(true, false)
        results.forEach { result ->
            server.enqueue {
                code(200)
                body(json.encodeToString(Box(result)))
            }
            assertThat(step.buzz(token).await(), "response").isEqualTo(result)
            server.verifyBuzz(token, breakoutId)
        }
    }

    @Test
    fun `clearBuzz throws IllegalStateException`() = runTestForAll { breakoutId, step ->
        server.enqueue { code(500) }
        assertFailure { step.clearBuzz(token).await() }.isInstanceOf<IllegalStateException>()
        server.verifyClearBuzz(token, breakoutId)
    }

    @Test
    fun `clearBuzz throws NoSuchNodeException`() = runTestForAll { breakoutId, step ->
        server.enqueue { code(404) }
        assertFailure { step.clearBuzz(token).await() }.isInstanceOf<NoSuchNodeException>()
        server.verifyClearBuzz(token, breakoutId)
    }

    @Test
    fun `clearBuzz throws NoSuchConferenceException`() = runTestForAll { breakoutId, step ->
        val message = "Neither conference nor gateway found"
        server.enqueue {
            code(404)
            body(json.encodeToString(Box(message)))
        }
        assertFailure { step.clearBuzz(token).await() }
            .isInstanceOf<NoSuchConferenceException>()
            .hasMessage(message)
        server.verifyClearBuzz(token, breakoutId)
    }

    @Test
    fun `clearBuzz throws InvalidTokenException`() = runTestForAll { breakoutId, step ->
        val message = "Invalid token"
        server.enqueue {
            code(403)
            body(json.encodeToString(Box(message)))
        }
        assertFailure { step.clearBuzz(token).await() }
            .isInstanceOf<InvalidTokenException>()
            .hasMessage(message)
        server.verifyClearBuzz(token, breakoutId)
    }

    @Test
    fun `clearBuzz returns result on 200`() = runTestForAll { breakoutId, step ->
        val results = listOf(true, false)
        results.forEach { result ->
            server.enqueue {
                code(200)
                body(json.encodeToString(Box(result)))
            }
            assertThat(step.clearBuzz(token).await(), "response").isEqualTo(result)
            server.verifyClearBuzz(token, breakoutId)
        }
    }

    @Test
    fun `preferredAspectRatio throws IllegalStateException`() = runTestForAll { breakoutId, step ->
        server.enqueue { code(500) }
        val request = Random.nextPreferredAspectRatioRequest()
        assertFailure { step.preferredAspectRatio(request, token).await() }
            .isInstanceOf<IllegalStateException>()
        server.verifyPreferredAspectRatio(request, token, breakoutId)
    }

    @Test
    fun `preferredAspectRatio throws NoSuchNodeException`() = runTestForAll { breakoutId, step ->
        server.enqueue { code(404) }
        val request = Random.nextPreferredAspectRatioRequest()
        assertFailure { step.preferredAspectRatio(request, token).await() }
            .isInstanceOf<NoSuchNodeException>()
        server.verifyPreferredAspectRatio(request, token, breakoutId)
    }

    @Test
    fun `preferredAspectRatio throws NoSuchConferenceException`() =
        runTestForAll { breakoutId, step ->
            val message = "Neither conference nor gateway found"
            server.enqueue {
                code(404)
                body(json.encodeToString(Box(message)))
            }
            val request = Random.nextPreferredAspectRatioRequest()
            assertFailure { step.preferredAspectRatio(request, token).await() }
                .isInstanceOf<NoSuchConferenceException>()
                .hasMessage(message)
            server.verifyPreferredAspectRatio(request, token, breakoutId)
        }

    @Test
    fun `preferredAspectRatio throws InvalidTokenException`() = runTestForAll { breakoutId, step ->
        val message = "Invalid token"
        server.enqueue {
            code(403)
            body(json.encodeToString(Box(message)))
        }
        val request = Random.nextPreferredAspectRatioRequest()
        assertFailure { step.preferredAspectRatio(request, token).await() }
            .isInstanceOf<InvalidTokenException>()
            .hasMessage(message)
        server.verifyPreferredAspectRatio(request, token, breakoutId)
    }

    @Test
    fun `preferredAspectRatio returns result on 200`() = runTestForAll { breakoutId, step ->
        val results = listOf(true, false)
        results.forEach { result ->
            server.enqueue {
                code(200)
                body(json.encodeToString(Box(result)))
            }
            val request = Random.nextPreferredAspectRatioRequest()
            assertThat(step.preferredAspectRatio(request, token).await(), "response")
                .isEqualTo(result)
            server.verifyPreferredAspectRatio(request, token, breakoutId)
        }
    }

    @Test
    fun `message throws IllegalStateException`() = runTestForAll { breakoutId, step ->
        server.enqueue { code(500) }
        val request = Random.nextMessageRequest()
        assertFailure { step.message(request, token).await() }.isInstanceOf<IllegalStateException>()
        server.verifyMessage(request, token, breakoutId)
    }

    @Test
    fun `message throws NoSuchNodeException`() = runTestForAll { breakoutId, step ->
        server.enqueue { code(404) }
        val request = Random.nextMessageRequest()
        assertFailure { step.message(request, token).await() }.isInstanceOf<NoSuchNodeException>()
        server.verifyMessage(request, token, breakoutId)
    }

    @Test
    fun `message throws NoSuchConferenceException`() = runTestForAll { breakoutId, step ->
        val message = "Neither conference nor gateway found"
        server.enqueue {
            code(404)
            body(json.encodeToString(Box(message)))
        }
        val request = Random.nextMessageRequest()
        assertFailure { step.message(request, token).await() }
            .isInstanceOf<NoSuchConferenceException>()
            .hasMessage(message)
        server.verifyMessage(request, token, breakoutId)
    }

    @Test
    fun `message throws InvalidTokenException`() = runTestForAll { breakoutId, step ->
        val message = "Invalid token"
        server.enqueue {
            code(403)
            body(json.encodeToString(Box(message)))
        }
        val request = Random.nextMessageRequest()
        assertFailure { step.message(request, token).await() }
            .isInstanceOf<InvalidTokenException>()
            .hasMessage(message)
        server.verifyMessage(request, token, breakoutId)
    }

    @Test
    fun `message returns result on 200`() = runTestForAll { breakoutId, step ->
        val results = listOf(true, false)
        results.forEach { result ->
            server.enqueue {
                code(200)
                body(json.encodeToString(Box(result)))
            }
            val request = Random.nextMessageRequest()
            assertThat(step.message(request, token).await(), "response").isEqualTo(result)
            server.verifyMessage(request, token, breakoutId)
        }
    }

    @Test
    fun `unlock throws IllegalStateException`() = runTestForAll { breakoutId, step ->
        server.enqueue { code(500) }
        assertFailure { step.unlock(token).await() }.isInstanceOf<IllegalStateException>()
        server.verifyUnlock(token, breakoutId)
    }

    @Test
    fun `unlock throws NoSuchNodeException`() = runTestForAll { breakoutId, step ->
        server.enqueue { code(404) }
        assertFailure { step.unlock(token).await() }.isInstanceOf<NoSuchNodeException>()
        server.verifyUnlock(token, breakoutId)
    }

    @Test
    fun `unlock throws NoSuchConferenceException`() = runTestForAll { breakoutId, step ->
        val message = "Neither conference nor gateway found"
        server.enqueue {
            code(404)
            body(json.encodeToString(Box(message)))
        }
        assertFailure { step.unlock(token).await() }
            .isInstanceOf<NoSuchConferenceException>()
            .hasMessage(message)
        server.verifyUnlock(token, breakoutId)
    }

    @Test
    fun `unlock throws InvalidTokenException`() = runTestForAll { breakoutId, step ->
        val message = "Invalid token"
        server.enqueue {
            code(403)
            body(json.encodeToString(Box(message)))
        }
        assertFailure { step.unlock(token).await() }
            .isInstanceOf<InvalidTokenException>()
            .hasMessage(message)
        server.verifyUnlock(token, breakoutId)
    }

    @Test
    fun `unlock returns result on 200`() = runTestForAll { breakoutId, step ->
        val results = listOf(true, false)
        results.forEach { result ->
            server.enqueue {
                code(200)
                body(json.encodeToString(Box(result)))
            }
            assertThat(step.unlock(token).await(), "result").isEqualTo(result)
            server.verifyUnlock(token, breakoutId)
        }
    }

    @Test
    fun `spotlightOn throws IllegalStateException`() = runTestForAll { breakoutId, step ->
        server.enqueue { code(500) }
        assertFailure { step.spotlightOn(token).await() }.isInstanceOf<IllegalStateException>()
        server.verifySpotlightOn(token, breakoutId)
    }

    @Test
    fun `spotlightOn throws NoSuchNodeException`() = runTestForAll { breakoutId, step ->
        server.enqueue { code(404) }
        assertFailure { step.spotlightOn(token).await() }.isInstanceOf<NoSuchNodeException>()
        server.verifySpotlightOn(token, breakoutId)
    }

    @Test
    fun `spotlightOn throws NoSuchConferenceException`() = runTestForAll { breakoutId, step ->
        val message = "Neither conference nor gateway found"
        server.enqueue {
            code(404)
            body(json.encodeToString(Box(message)))
        }
        assertFailure { step.spotlightOn(token).await() }
            .isInstanceOf<NoSuchConferenceException>()
            .hasMessage(message)
        server.verifySpotlightOn(token, breakoutId)
    }

    @Test
    fun `spotlightOn throws InvalidTokenException`() = runTestForAll { breakoutId, step ->
        val message = "Invalid token"
        server.enqueue {
            code(403)
            body(json.encodeToString(Box(message)))
        }
        assertFailure { step.spotlightOn(token).await() }
            .isInstanceOf<InvalidTokenException>()
            .hasMessage(message)
        server.verifySpotlightOn(token, breakoutId)
    }

    @Test
    fun `spotlightOn returns result on 200`() = runTestForAll { breakoutId, step ->
        val results = listOf(true, false)
        results.forEach { result ->
            server.enqueue {
                code(200)
                body(json.encodeToString(Box(result)))
            }
            assertThat(step.spotlightOn(token).await(), "result").isEqualTo(result)
            server.verifySpotlightOn(token, breakoutId)
        }
    }

    @Test
    fun `spotlightOff throws IllegalStateException`() = runTestForAll { breakoutId, step ->
        server.enqueue { code(500) }
        assertFailure { step.spotlightOff(token).await() }.isInstanceOf<IllegalStateException>()
        server.verifySpotlightOff(token, breakoutId)
    }

    @Test
    fun `spotlightOff throws NoSuchNodeException`() = runTestForAll { breakoutId, step ->
        server.enqueue { code(404) }
        assertFailure { step.spotlightOff(token).await() }.isInstanceOf<NoSuchNodeException>()
        server.verifySpotlightOff(token, breakoutId)
    }

    @Test
    fun `spotlightOff throws NoSuchConferenceException`() = runTestForAll { breakoutId, step ->
        val message = "Neither conference nor gateway found"
        server.enqueue {
            code(404)
            body(json.encodeToString(Box(message)))
        }
        assertFailure { step.spotlightOff(token).await() }
            .isInstanceOf<NoSuchConferenceException>()
            .hasMessage(message)
        server.verifySpotlightOff(token, breakoutId)
    }

    @Test
    fun `spotlightOff throws InvalidTokenException`() = runTestForAll { breakoutId, step ->
        val message = "Invalid token"
        server.enqueue {
            code(403)
            body(json.encodeToString(Box(message)))
        }
        assertFailure { step.spotlightOff(token).await() }
            .isInstanceOf<InvalidTokenException>()
            .hasMessage(message)
        server.verifySpotlightOff(token, breakoutId)
    }

    @Test
    fun `spotlightOff returns result on 200`() = runTestForAll { breakoutId, step ->
        val results = listOf(true, false)
        results.forEach { result ->
            server.enqueue {
                code(200)
                body(json.encodeToString(Box(result)))
            }
            assertThat(step.spotlightOff(token).await(), "result").isEqualTo(result)
            server.verifySpotlightOff(token, breakoutId)
        }
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
    fun `disconnect returns result on 200`() = runTestForAll { breakoutId, step ->
        val results = listOf(true, false)
        results.forEach { result ->
            server.enqueue {
                code(200)
                body(json.encodeToString(Box(result)))
            }
            assertThat(step.disconnect(token).await(), "result").isEqualTo(result)
            server.verifyDisconnect(token, breakoutId)
        }
    }

    private fun runTestForAll(
        testBody: suspend TestScope.(BreakoutId?, InfinityService.ParticipantStep) -> Unit,
    ) = steps.forAll { breakoutId, step -> runTest { testBody(breakoutId, step) } }

    private fun MockWebServer.verifyCalls(
        request: CallsRequest,
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
            addPathSegment("dtmf")
        }
        assertToken(token)
        assertPost(json, request)
    }

    private fun MockWebServer.verifyMute(token: Token, breakoutId: BreakoutId?) = takeRequest {
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
            addPathSegment("mute")
        }
        assertToken(token)
        assertPostEmptyBody()
    }

    private fun MockWebServer.verifyUnmute(token: Token, breakoutId: BreakoutId?) = takeRequest {
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
            addPathSegment("unmute")
        }
        assertToken(token)
        assertPostEmptyBody()
    }

    private fun MockWebServer.verifyClientMute(token: Token, breakoutId: BreakoutId?) =
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
                addPathSegment("client_mute")
            }
            assertToken(token)
            assertPostEmptyBody()
        }

    private fun MockWebServer.verifyClientUnmute(token: Token, breakoutId: BreakoutId?) =
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
                addPathSegment("client_unmute")
            }
            assertToken(token)
            assertPostEmptyBody()
        }

    private fun MockWebServer.verifyVideoMuted(token: Token, breakoutId: BreakoutId?) =
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
                addPathSegment("video_muted")
            }
            assertToken(token)
            assertPostEmptyBody()
        }

    private fun MockWebServer.verifyVideoUnMuted(token: Token, breakoutId: BreakoutId?) =
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
                addPathSegment("video_unmuted")
            }
            assertToken(token)
            assertPostEmptyBody()
        }

    private fun MockWebServer.verifyTakeFloor(token: Token, breakoutId: BreakoutId?) = takeRequest {
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
            addPathSegment("take_floor")
        }
        assertToken(token)
        assertPostEmptyBody()
    }

    private fun MockWebServer.verifyReleaseFloor(token: Token, breakoutId: BreakoutId?) =
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
                addPathSegment("release_floor")
            }
            assertToken(token)
            assertPostEmptyBody()
        }

    private fun MockWebServer.verifyMessage(
        request: MessageRequest,
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
            addPathSegment("message")
        }
        assertToken(token)
        assertPost(json, request)
    }

    private fun MockWebServer.verifyPreferredAspectRatio(
        request: PreferredAspectRatioRequest,
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
            addPathSegment("preferred_aspect_ratio")
        }
        assertToken(token)
        assertPost(json, request)
    }

    private fun Random.nextCallsRequest() = CallsRequest(
        sdp = nextString(),
        present = nextString(),
        callType = nextString(),
        fecc = nextBoolean(),
    )

    private fun Random.nextPreferredAspectRatioRequest() = PreferredAspectRatioRequest(nextFloat())

    private fun MockWebServer.verifyBuzz(token: Token, breakoutId: BreakoutId?) = takeRequest {
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
            addPathSegment("buzz")
        }
        assertToken(token)
        assertPostEmptyBody()
    }

    private fun MockWebServer.verifyClearBuzz(token: Token, breakoutId: BreakoutId?) = takeRequest {
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
            addPathSegment("clearbuzz")
        }
        assertToken(token)
        assertPostEmptyBody()
    }

    private fun MockWebServer.verifyUnlock(token: Token, breakoutId: BreakoutId?) = takeRequest {
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
            addPathSegment("unlock")
        }
        assertToken(token)
        assertPostEmptyBody()
    }

    private fun MockWebServer.verifySpotlightOn(token: Token, breakoutId: BreakoutId?) =
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
                addPathSegment("spotlighton")
            }
            assertToken(token)
            assertPostEmptyBody()
        }

    private fun MockWebServer.verifySpotlightOff(token: Token, breakoutId: BreakoutId?) =
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
                addPathSegment("spotlightoff")
            }
            assertToken(token)
            assertPostEmptyBody()
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
                addPathSegment("disconnect")
            }
            assertToken(token)
            assertPostEmptyBody()
        }
}
