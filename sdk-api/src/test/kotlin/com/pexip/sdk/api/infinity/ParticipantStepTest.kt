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
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import com.pexip.sdk.api.coroutines.await
import com.pexip.sdk.api.infinity.internal.addPathSegment
import com.pexip.sdk.infinity.ParticipantId
import com.pexip.sdk.infinity.test.nextCallId
import com.pexip.sdk.infinity.test.nextParticipantId
import com.pexip.sdk.infinity.test.nextString
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl
import okhttp3.mockwebserver.MockWebServer
import org.junit.Rule
import kotlin.properties.Delegates
import kotlin.random.Random
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

internal class ParticipantStepTest {

    @get:Rule
    val rule = SecureMockWebServerRule()

    private val server get() = rule.server
    private val client get() = rule.client

    private lateinit var node: HttpUrl
    private lateinit var conferenceAlias: String
    private lateinit var json: Json
    private lateinit var token: Token
    private lateinit var step: InfinityService.ParticipantStep

    private var participantId: ParticipantId by Delegates.notNull()

    @BeforeTest
    fun setUp() {
        node = server.url("/")
        conferenceAlias = Random.nextString()
        participantId = Random.nextParticipantId()
        json = InfinityService.Json
        token = Random.nextFakeToken()
        step = InfinityService.create(client, json)
            .newRequest(node)
            .conference(conferenceAlias)
            .participant(participantId)
    }

    @Test
    fun `calls throws IllegalStateException`() {
        server.enqueue { setResponseCode(500) }
        val request = Random.nextCallsRequest()
        assertFailsWith<IllegalStateException> { step.calls(request, token).execute() }
        server.verifyCalls(request, token)
    }

    @Test
    fun `calls throws NoSuchNodeException`() {
        server.enqueue { setResponseCode(404) }
        val request = Random.nextCallsRequest()
        assertFailsWith<NoSuchNodeException> { step.calls(request, token).execute() }
        server.verifyCalls(request, token)
    }

    @Test
    fun `calls throws NoSuchConferenceException`() {
        val message = "Neither conference nor gateway found"
        server.enqueue {
            setResponseCode(404)
            setBody(json.encodeToString(Box(message)))
        }
        val request = Random.nextCallsRequest()
        assertFailsWith<NoSuchConferenceException> { step.calls(request, token).execute() }
        server.verifyCalls(request, token)
    }

    @Test
    fun `calls throws InvalidTokenException`() {
        val message = "Invalid token"
        server.enqueue {
            setResponseCode(403)
            setBody(json.encodeToString(Box(message)))
        }
        val request = Random.nextCallsRequest()
        assertFailsWith<InvalidTokenException> { step.calls(request, token).execute() }
        server.verifyCalls(request, token)
    }

    @Test
    fun `calls returns CallsResponse`() {
        val response = CallsResponse(
            callId = Random.nextCallId(),
            sdp = Random.nextString(),
        )
        server.enqueue {
            setResponseCode(200)
            setBody(json.encodeToString(Box(response)))
        }
        val request = Random.nextCallsRequest()
        assertEquals(response, step.calls(request, token).execute())
        server.verifyCalls(request, token)
    }

    @Test
    fun `dtmf throws IllegalStateException`() {
        server.enqueue { setResponseCode(500) }
        val request = DtmfRequest(Random.nextDigits())
        assertFailsWith<IllegalStateException> { step.dtmf(request, token).execute() }
        server.verifyDtmf(request, token)
    }

    @Test
    fun `dtmf throws NoSuchNodeException`() {
        server.enqueue { setResponseCode(404) }
        val request = DtmfRequest(Random.nextDigits())
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
        val request = DtmfRequest(Random.nextDigits())
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
        val request = DtmfRequest(Random.nextDigits())
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
        val request = DtmfRequest(Random.nextDigits())
        assertEquals(response, step.dtmf(request, token).execute())
        server.verifyDtmf(request, token)
    }

    @Test
    fun `mute throws IllegalStateException`() {
        server.enqueue { setResponseCode(500) }
        assertFailsWith<IllegalStateException> { step.mute(token).execute() }
        server.verifyMute(token)
    }

    @Test
    fun `mute throws NoSuchNodeException`() {
        server.enqueue { setResponseCode(404) }
        assertFailsWith<NoSuchNodeException> { step.mute(token).execute() }
        server.verifyMute(token)
    }

    @Test
    fun `mute throws NoSuchConferenceException`() {
        val message = "Neither conference nor gateway found"
        server.enqueue {
            setResponseCode(404)
            setBody(json.encodeToString(Box(message)))
        }
        assertFailsWith<NoSuchConferenceException> { step.mute(token).execute() }
        server.verifyMute(token)
    }

    @Test
    fun `mute throws InvalidTokenException`() {
        val message = "Invalid token"
        server.enqueue {
            setResponseCode(403)
            setBody(json.encodeToString(Box(message)))
        }
        assertFailsWith<InvalidTokenException> { step.mute(token).execute() }
        server.verifyMute(token)
    }

    @Test
    fun `mute returns`() {
        server.enqueue { setResponseCode(200) }
        step.mute(token).execute()
        server.verifyMute(token)
    }

    @Test
    fun `unmute throws IllegalStateException`() {
        server.enqueue { setResponseCode(500) }
        assertFailsWith<IllegalStateException> { step.unmute(token).execute() }
        server.verifyUnmute(token)
    }

    @Test
    fun `unmute throws NoSuchNodeException`() {
        server.enqueue { setResponseCode(404) }
        assertFailsWith<NoSuchNodeException> { step.unmute(token).execute() }
        server.verifyUnmute(token)
    }

    @Test
    fun `unmute throws NoSuchConferenceException`() {
        val message = "Neither conference nor gateway found"
        server.enqueue {
            setResponseCode(404)
            setBody(json.encodeToString(Box(message)))
        }
        assertFailsWith<NoSuchConferenceException> { step.unmute(token).execute() }
        server.verifyUnmute(token)
    }

    @Test
    fun `unmute throws InvalidTokenException`() {
        val message = "Invalid token"
        server.enqueue {
            setResponseCode(403)
            setBody(json.encodeToString(Box(message)))
        }
        assertFailsWith<InvalidTokenException> { step.unmute(token).execute() }
        server.verifyUnmute(token)
    }

    @Test
    fun `unmute returns`() {
        server.enqueue { setResponseCode(200) }
        step.unmute(token).execute()
        server.verifyUnmute(token)
    }

    @Test
    fun `videoMuted throws IllegalStateException`() {
        server.enqueue { setResponseCode(500) }
        assertFailsWith<IllegalStateException> { step.videoMuted(token).execute() }
        server.verifyVideoMuted(token)
    }

    @Test
    fun `videoMuted throws NoSuchNodeException`() {
        server.enqueue { setResponseCode(404) }
        assertFailsWith<NoSuchNodeException> { step.videoMuted(token).execute() }
        server.verifyVideoMuted(token)
    }

    @Test
    fun `videoMuted throws NoSuchConferenceException`() {
        val message = "Neither conference nor gateway found"
        server.enqueue {
            setResponseCode(404)
            setBody(json.encodeToString(Box(message)))
        }
        assertFailsWith<NoSuchConferenceException> { step.videoMuted(token).execute() }
        server.verifyVideoMuted(token)
    }

    @Test
    fun `videoMuted throws InvalidTokenException`() {
        val message = "Invalid token"
        server.enqueue {
            setResponseCode(403)
            setBody(json.encodeToString(Box(message)))
        }
        assertFailsWith<InvalidTokenException> { step.videoMuted(token).execute() }
        server.verifyVideoMuted(token)
    }

    @Test
    fun `videoMuted returns`() {
        server.enqueue { setResponseCode(200) }
        step.videoMuted(token).execute()
        server.verifyVideoMuted(token)
    }

    @Test
    fun `videoUnmuted throws IllegalStateException`() {
        server.enqueue { setResponseCode(500) }
        assertFailsWith<IllegalStateException> { step.videoUnmuted(token).execute() }
        server.verifyVideoUnMuted(token)
    }

    @Test
    fun `videoUnmuted throws NoSuchNodeException`() {
        server.enqueue { setResponseCode(404) }
        assertFailsWith<NoSuchNodeException> { step.videoUnmuted(token).execute() }
        server.verifyVideoUnMuted(token)
    }

    @Test
    fun `videoUnmuted throws NoSuchConferenceException`() {
        val message = "Neither conference nor gateway found"
        server.enqueue {
            setResponseCode(404)
            setBody(json.encodeToString(Box(message)))
        }
        assertFailsWith<NoSuchConferenceException> { step.videoUnmuted(token).execute() }
        server.verifyVideoUnMuted(token)
    }

    @Test
    fun `videoUnmuted throws InvalidTokenException`() {
        val message = "Invalid token"
        server.enqueue {
            setResponseCode(403)
            setBody(json.encodeToString(Box(message)))
        }
        assertFailsWith<InvalidTokenException> { step.videoUnmuted(token).execute() }
        server.verifyVideoUnMuted(token)
    }

    @Test
    fun `videoUnmuted returns`() {
        server.enqueue { setResponseCode(200) }
        step.videoUnmuted(token).execute()
        server.verifyVideoUnMuted(token)
    }

    @Test
    fun `takeFloor throws IllegalStateException`() {
        server.enqueue { setResponseCode(500) }
        assertFailsWith<IllegalStateException> { step.takeFloor(token).execute() }
        server.verifyTakeFloor(token)
    }

    @Test
    fun `takeFloor throws NoSuchNodeException`() {
        server.enqueue { setResponseCode(404) }
        assertFailsWith<NoSuchNodeException> { step.takeFloor(token).execute() }
        server.verifyTakeFloor(token)
    }

    @Test
    fun `takeFloor throws NoSuchConferenceException`() {
        val message = "Neither conference nor gateway found"
        server.enqueue {
            setResponseCode(404)
            setBody(json.encodeToString(Box(message)))
        }
        assertFailsWith<NoSuchConferenceException> { step.takeFloor(token).execute() }
        server.verifyTakeFloor(token)
    }

    @Test
    fun `takeFloor throws InvalidTokenException`() {
        val message = "Invalid token"
        server.enqueue {
            setResponseCode(403)
            setBody(json.encodeToString(Box(message)))
        }
        assertFailsWith<InvalidTokenException> { step.takeFloor(token).execute() }
        server.verifyTakeFloor(token)
    }

    @Test
    fun `takeFloor returns`() {
        server.enqueue { setResponseCode(200) }
        step.takeFloor(token).execute()
        server.verifyTakeFloor(token)
    }

    @Test
    fun `releaseFloor throws IllegalStateException`() {
        server.enqueue { setResponseCode(500) }
        assertFailsWith<IllegalStateException> { step.releaseFloor(token).execute() }
        server.verifyReleaseFloor(token)
    }

    @Test
    fun `releaseFloor throws NoSuchNodeException`() {
        server.enqueue { setResponseCode(404) }
        assertFailsWith<NoSuchNodeException> { step.releaseFloor(token).execute() }
        server.verifyReleaseFloor(token)
    }

    @Test
    fun `releaseFloor throws NoSuchConferenceException`() {
        val message = "Neither conference nor gateway found"
        server.enqueue {
            setResponseCode(404)
            setBody(json.encodeToString(Box(message)))
        }
        assertFailsWith<NoSuchConferenceException> { step.releaseFloor(token).execute() }
        server.verifyReleaseFloor(token)
    }

    @Test
    fun `releaseFloor throws InvalidTokenException`() {
        val message = "Invalid token"
        server.enqueue {
            setResponseCode(403)
            setBody(json.encodeToString(Box(message)))
        }
        assertFailsWith<InvalidTokenException> { step.releaseFloor(token).execute() }
        server.verifyReleaseFloor(token)
    }

    @Test
    fun `releaseFloor returns`() {
        server.enqueue { setResponseCode(200) }
        step.releaseFloor(token).execute()
        server.verifyReleaseFloor(token)
    }

    @Test
    fun `buzz throws IllegalStateException`() = runTest {
        server.enqueue { setResponseCode(500) }
        assertFailure { step.buzz(token).await() }.isInstanceOf<IllegalStateException>()
        server.verifyBuzz(token)
    }

    @Test
    fun `buzz throws NoSuchNodeException`() = runTest {
        server.enqueue { setResponseCode(404) }
        assertFailure { step.buzz(token).await() }.isInstanceOf<NoSuchNodeException>()
        server.verifyBuzz(token)
    }

    @Test
    fun `buzz throws NoSuchConferenceException`() = runTest {
        val message = "Neither conference nor gateway found"
        server.enqueue {
            setResponseCode(404)
            setBody(json.encodeToString(Box(message)))
        }
        assertFailure { step.buzz(token).await() }.isInstanceOf<NoSuchConferenceException>()
        server.verifyBuzz(token)
    }

    @Test
    fun `buzz throws InvalidTokenException`() = runTest {
        val message = "Invalid token"
        server.enqueue {
            setResponseCode(403)
            setBody(json.encodeToString(Box(message)))
        }
        assertFailure { step.buzz(token).await() }.isInstanceOf<InvalidTokenException>()
        server.verifyBuzz(token)
    }

    @Test
    fun `buzz returns result on 200`() = runTest {
        val results = listOf(true, false)
        results.forEach { result ->
            server.enqueue {
                setResponseCode(200)
                setBody(json.encodeToString(Box(result)))
            }
            assertThat(step.buzz(token).await(), "result").isEqualTo(result)
            server.verifyBuzz(token)
        }
    }

    @Test
    fun `clearBuzz throws IllegalStateException`() = runTest {
        server.enqueue { setResponseCode(500) }
        assertFailure { step.clearBuzz(token).await() }.isInstanceOf<IllegalStateException>()
        server.verifyClearBuzz(token)
    }

    @Test
    fun `clearBuzz throws NoSuchNodeException`() = runTest {
        server.enqueue { setResponseCode(404) }
        assertFailure { step.clearBuzz(token).await() }.isInstanceOf<NoSuchNodeException>()
        server.verifyClearBuzz(token)
    }

    @Test
    fun `clearBuzz throws NoSuchConferenceException`() = runTest {
        val message = "Neither conference nor gateway found"
        server.enqueue {
            setResponseCode(404)
            setBody(json.encodeToString(Box(message)))
        }
        assertFailure { step.clearBuzz(token).await() }.isInstanceOf<NoSuchConferenceException>()
        server.verifyClearBuzz(token)
    }

    @Test
    fun `clearBuzz throws InvalidTokenException`() = runTest {
        val message = "Invalid token"
        server.enqueue {
            setResponseCode(403)
            setBody(json.encodeToString(Box(message)))
        }
        assertFailure { step.clearBuzz(token).await() }.isInstanceOf<InvalidTokenException>()
        server.verifyClearBuzz(token)
    }

    @Test
    fun `clearBuzz returns result on 200`() = runTest {
        val results = listOf(true, false)
        results.forEach { result ->
            server.enqueue {
                setResponseCode(200)
                setBody(json.encodeToString(Box(result)))
            }
            assertThat(step.clearBuzz(token).await(), "result").isEqualTo(result)
            server.verifyClearBuzz(token)
        }
    }

    @Test
    fun `preferredAspectRatio throws IllegalStateException`() {
        server.enqueue { setResponseCode(500) }
        val request = Random.nextPreferredAspectRatioRequest()
        assertFailsWith<IllegalStateException> {
            step.preferredAspectRatio(request, token).execute()
        }
        server.verifyPreferredAspectRatio(request, token)
    }

    @Test
    fun `preferredAspectRatio throws NoSuchNodeException`() {
        server.enqueue { setResponseCode(404) }
        val request = Random.nextPreferredAspectRatioRequest()
        assertFailsWith<NoSuchNodeException> { step.preferredAspectRatio(request, token).execute() }
        server.verifyPreferredAspectRatio(request, token)
    }

    @Test
    fun `preferredAspectRatio throws NoSuchConferenceException`() {
        val message = "Neither conference nor gateway found"
        server.enqueue {
            setResponseCode(404)
            setBody(json.encodeToString(Box(message)))
        }
        val request = Random.nextPreferredAspectRatioRequest()
        assertFailsWith<NoSuchConferenceException> {
            step.preferredAspectRatio(request, token).execute()
        }
        server.verifyPreferredAspectRatio(request, token)
    }

    @Test
    fun `preferredAspectRatio throws InvalidTokenException`() {
        val message = "Invalid token"
        server.enqueue {
            setResponseCode(403)
            setBody(json.encodeToString(Box(message)))
        }
        val request = Random.nextPreferredAspectRatioRequest()
        assertFailsWith<InvalidTokenException> {
            step.preferredAspectRatio(request, token).execute()
        }
        server.verifyPreferredAspectRatio(request, token)
    }

    @Test
    fun `preferredAspectRatio returns result on 200`() {
        val results = listOf(true, false)
        results.forEach { result ->
            server.enqueue {
                setResponseCode(200)
                setBody(json.encodeToString(Box(result)))
            }
            val request = Random.nextPreferredAspectRatioRequest()
            assertEquals(result, step.preferredAspectRatio(request, token).execute())
            server.verifyPreferredAspectRatio(request, token)
        }
    }

    @Test
    fun `message throws IllegalStateException`() {
        server.enqueue { setResponseCode(500) }
        val request = Random.nextMessageRequest()
        assertFailsWith<IllegalStateException> { step.message(request, token).execute() }
        server.verifyMessage(request, token)
    }

    @Test
    fun `message throws NoSuchNodeException`() {
        server.enqueue { setResponseCode(404) }
        val request = Random.nextMessageRequest()
        assertFailsWith<NoSuchNodeException> { step.message(request, token).execute() }
        server.verifyMessage(request, token)
    }

    @Test
    fun `message throws NoSuchConferenceException`() {
        val message = "Neither conference nor gateway found"
        server.enqueue {
            setResponseCode(404)
            setBody(json.encodeToString(Box(message)))
        }
        val request = Random.nextMessageRequest()
        assertFailsWith<NoSuchConferenceException> { step.message(request, token).execute() }
        server.verifyMessage(request, token)
    }

    @Test
    fun `message throws InvalidTokenException`() {
        val message = "Invalid token"
        server.enqueue {
            setResponseCode(403)
            setBody(json.encodeToString(Box(message)))
        }
        val request = Random.nextMessageRequest()
        assertFailsWith<InvalidTokenException> { step.message(request, token).execute() }
        server.verifyMessage(request, token)
    }

    @Test
    fun `message returns result on 200`() {
        val results = listOf(true, false)
        results.forEach { result ->
            server.enqueue {
                setResponseCode(200)
                setBody(json.encodeToString(Box(result)))
            }
            val request = Random.nextMessageRequest()
            assertEquals(result, step.message(request, token).execute())
            server.verifyMessage(request, token)
        }
    }

    @Test
    fun `unlock throws IllegalStateException`() = runTest {
        server.enqueue { setResponseCode(500) }
        assertFailure { step.unlock(token).await() }.isInstanceOf<IllegalStateException>()
        server.verifyUnlock(token)
    }

    @Test
    fun `unlock throws NoSuchNodeException`() = runTest {
        server.enqueue { setResponseCode(404) }
        assertFailure { step.unlock(token).await() }.isInstanceOf<NoSuchNodeException>()
        server.verifyUnlock(token)
    }

    @Test
    fun `unlock throws NoSuchConferenceException`() = runTest {
        val message = "Neither conference nor gateway found"
        server.enqueue {
            setResponseCode(404)
            setBody(json.encodeToString(Box(message)))
        }
        assertFailure { step.unlock(token).await() }.isInstanceOf<NoSuchConferenceException>()
        server.verifyUnlock(token)
    }

    @Test
    fun `unlock throws InvalidTokenException`() = runTest {
        val message = "Invalid token"
        server.enqueue {
            setResponseCode(403)
            setBody(json.encodeToString(Box(message)))
        }
        assertFailure { step.unlock(token).await() }.isInstanceOf<InvalidTokenException>()
        server.verifyUnlock(token)
    }

    @Test
    fun `unlock returns result on 200`() = runTest {
        val results = listOf(true, false)
        results.forEach { result ->
            server.enqueue {
                setResponseCode(200)
                setBody(json.encodeToString(Box(result)))
            }
            assertThat(step.unlock(token).await(), "result").isEqualTo(result)
            server.verifyUnlock(token)
        }
    }

    @Test
    fun `spotlightOn throws IllegalStateException`() = runTest {
        server.enqueue { setResponseCode(500) }
        assertFailure { step.spotlightOn(token).await() }.isInstanceOf<IllegalStateException>()
        server.verifySpotlightOn(token)
    }

    @Test
    fun `spotlightOn throws NoSuchNodeException`() = runTest {
        server.enqueue { setResponseCode(404) }
        assertFailure { step.spotlightOn(token).await() }.isInstanceOf<NoSuchNodeException>()
        server.verifySpotlightOn(token)
    }

    @Test
    fun `spotlightOn throws NoSuchConferenceException`() = runTest {
        val message = "Neither conference nor gateway found"
        server.enqueue {
            setResponseCode(404)
            setBody(json.encodeToString(Box(message)))
        }
        assertFailure { step.spotlightOn(token).await() }.isInstanceOf<NoSuchConferenceException>()
        server.verifySpotlightOn(token)
    }

    @Test
    fun `spotlightOn throws InvalidTokenException`() = runTest {
        val message = "Invalid token"
        server.enqueue {
            setResponseCode(403)
            setBody(json.encodeToString(Box(message)))
        }
        assertFailure { step.spotlightOn(token).await() }.isInstanceOf<InvalidTokenException>()
        server.verifySpotlightOn(token)
    }

    @Test
    fun `spotlightOn returns result on 200`() = runTest {
        val results = listOf(true, false)
        results.forEach { result ->
            server.enqueue {
                setResponseCode(200)
                setBody(json.encodeToString(Box(result)))
            }
            assertThat(step.spotlightOn(token).await(), "result").isEqualTo(result)
            server.verifySpotlightOn(token)
        }
    }

    @Test
    fun `spotlightOff throws IllegalStateException`() = runTest {
        server.enqueue { setResponseCode(500) }
        assertFailure { step.spotlightOff(token).await() }.isInstanceOf<IllegalStateException>()
        server.verifySpotlightOff(token)
    }

    @Test
    fun `spotlightOff throws NoSuchNodeException`() = runTest {
        server.enqueue { setResponseCode(404) }
        assertFailure { step.spotlightOff(token).await() }.isInstanceOf<NoSuchNodeException>()
        server.verifySpotlightOff(token)
    }

    @Test
    fun `spotlightOff throws NoSuchConferenceException`() = runTest {
        val message = "Neither conference nor gateway found"
        server.enqueue {
            setResponseCode(404)
            setBody(json.encodeToString(Box(message)))
        }
        assertFailure { step.spotlightOff(token).await() }.isInstanceOf<NoSuchConferenceException>()
        server.verifySpotlightOff(token)
    }

    @Test
    fun `spotlightOff throws InvalidTokenException`() = runTest {
        val message = "Invalid token"
        server.enqueue {
            setResponseCode(403)
            setBody(json.encodeToString(Box(message)))
        }
        assertFailure { step.spotlightOff(token).await() }.isInstanceOf<InvalidTokenException>()
        server.verifySpotlightOff(token)
    }

    @Test
    fun `spotlightOff returns result on 200`() = runTest {
        val results = listOf(true, false)
        results.forEach { result ->
            server.enqueue {
                setResponseCode(200)
                setBody(json.encodeToString(Box(result)))
            }
            assertThat(step.spotlightOff(token).await(), "result").isEqualTo(result)
            server.verifySpotlightOff(token)
        }
    }

    @Test
    fun `disconnect throws IllegalStateException`() = runTest {
        server.enqueue { setResponseCode(500) }
        assertFailure { step.disconnect(token).await() }.isInstanceOf<IllegalStateException>()
        server.verifyDisconnect(token)
    }

    @Test
    fun `disconnect throws NoSuchNodeException`() = runTest {
        server.enqueue { setResponseCode(404) }
        assertFailure { step.disconnect(token).await() }.isInstanceOf<NoSuchNodeException>()
        server.verifyDisconnect(token)
    }

    @Test
    fun `disconnect throws NoSuchConferenceException`() = runTest {
        val message = "Neither conference nor gateway found"
        server.enqueue {
            setResponseCode(404)
            setBody(json.encodeToString(Box(message)))
        }
        assertFailure { step.disconnect(token).await() }.isInstanceOf<NoSuchConferenceException>()
        server.verifyDisconnect(token)
    }

    @Test
    fun `disconnect throws InvalidTokenException`() = runTest {
        val message = "Invalid token"
        server.enqueue {
            setResponseCode(403)
            setBody(json.encodeToString(Box(message)))
        }
        assertFailure { step.disconnect(token).await() }.isInstanceOf<InvalidTokenException>()
        server.verifyDisconnect(token)
    }

    @Test
    fun `disconnect returns result on 200`() = runTest {
        val results = listOf(true, false)
        results.forEach { result ->
            server.enqueue {
                setResponseCode(200)
                setBody(json.encodeToString(Box(result)))
            }
            assertThat(step.disconnect(token).await(), "result").isEqualTo(result)
            server.verifyDisconnect(token)
        }
    }

    private fun MockWebServer.verifyCalls(request: CallsRequest, token: Token) = takeRequest {
        assertRequestUrl(node) {
            addPathSegments("api/client/v2")
            addPathSegment("conferences")
            addPathSegment(conferenceAlias)
            addPathSegment("participants")
            addPathSegment(participantId)
            addPathSegment("calls")
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
            addPathSegment("dtmf")
        }
        assertToken(token)
        assertPost(json, request)
    }

    private fun MockWebServer.verifyMute(token: Token) = takeRequest {
        assertRequestUrl(node) {
            addPathSegments("api/client/v2")
            addPathSegment("conferences")
            addPathSegment(conferenceAlias)
            addPathSegment("participants")
            addPathSegment(participantId)
            addPathSegment("mute")
        }
        assertToken(token)
        assertPostEmptyBody()
    }

    private fun MockWebServer.verifyUnmute(token: Token) = takeRequest {
        assertRequestUrl(node) {
            addPathSegments("api/client/v2")
            addPathSegment("conferences")
            addPathSegment(conferenceAlias)
            addPathSegment("participants")
            addPathSegment(participantId)
            addPathSegment("unmute")
        }
        assertToken(token)
        assertPostEmptyBody()
    }

    private fun MockWebServer.verifyVideoMuted(token: Token) = takeRequest {
        assertRequestUrl(node) {
            addPathSegments("api/client/v2")
            addPathSegment("conferences")
            addPathSegment(conferenceAlias)
            addPathSegment("participants")
            addPathSegment(participantId)
            addPathSegment("video_muted")
        }
        assertToken(token)
        assertPostEmptyBody()
    }

    private fun MockWebServer.verifyVideoUnMuted(token: Token) = takeRequest {
        assertRequestUrl(node) {
            addPathSegments("api/client/v2")
            addPathSegment("conferences")
            addPathSegment(conferenceAlias)
            addPathSegment("participants")
            addPathSegment(participantId)
            addPathSegment("video_unmuted")
        }
        assertToken(token)
        assertPostEmptyBody()
    }

    private fun MockWebServer.verifyTakeFloor(token: Token) = takeRequest {
        assertRequestUrl(node) {
            addPathSegments("api/client/v2")
            addPathSegment("conferences")
            addPathSegment(conferenceAlias)
            addPathSegment("participants")
            addPathSegment(participantId)
            addPathSegment("take_floor")
        }
        assertToken(token)
        assertPostEmptyBody()
    }

    private fun MockWebServer.verifyReleaseFloor(token: Token) = takeRequest {
        assertRequestUrl(node) {
            addPathSegments("api/client/v2")
            addPathSegment("conferences")
            addPathSegment(conferenceAlias)
            addPathSegment("participants")
            addPathSegment(participantId)
            addPathSegment("release_floor")
        }
        assertToken(token)
        assertPostEmptyBody()
    }

    private fun MockWebServer.verifyMessage(request: MessageRequest, token: Token) =
        takeRequest {
            assertRequestUrl(node) {
                addPathSegments("api/client/v2")
                addPathSegment("conferences")
                addPathSegment(conferenceAlias)
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
    ) = takeRequest {
        assertRequestUrl(node) {
            addPathSegments("api/client/v2")
            addPathSegment("conferences")
            addPathSegment(conferenceAlias)
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

    private fun MockWebServer.verifyBuzz(token: Token) = takeRequest {
        assertRequestUrl(node) {
            addPathSegments("api/client/v2")
            addPathSegment("conferences")
            addPathSegment(conferenceAlias)
            addPathSegment("participants")
            addPathSegment(participantId)
            addPathSegment("buzz")
        }
        assertToken(token)
        assertPostEmptyBody()
    }

    private fun MockWebServer.verifyClearBuzz(token: Token) = takeRequest {
        assertRequestUrl(node) {
            addPathSegments("api/client/v2")
            addPathSegment("conferences")
            addPathSegment(conferenceAlias)
            addPathSegment("participants")
            addPathSegment(participantId)
            addPathSegment("clearbuzz")
        }
        assertToken(token)
        assertPostEmptyBody()
    }

    private fun MockWebServer.verifyUnlock(token: Token) = takeRequest {
        assertRequestUrl(node) {
            addPathSegments("api/client/v2")
            addPathSegment("conferences")
            addPathSegment(conferenceAlias)
            addPathSegment("participants")
            addPathSegment(participantId)
            addPathSegment("unlock")
        }
        assertToken(token)
        assertPostEmptyBody()
    }

    private fun MockWebServer.verifySpotlightOn(token: Token) = takeRequest {
        assertRequestUrl(node) {
            addPathSegments("api/client/v2")
            addPathSegment("conferences")
            addPathSegment(conferenceAlias)
            addPathSegment("participants")
            addPathSegment(participantId)
            addPathSegment("spotlighton")
        }
        assertToken(token)
        assertPostEmptyBody()
    }

    private fun MockWebServer.verifySpotlightOff(token: Token) = takeRequest {
        assertRequestUrl(node) {
            addPathSegments("api/client/v2")
            addPathSegment("conferences")
            addPathSegment(conferenceAlias)
            addPathSegment("participants")
            addPathSegment(participantId)
            addPathSegment("spotlightoff")
        }
        assertToken(token)
        assertPostEmptyBody()
    }

    private fun MockWebServer.verifyDisconnect(token: Token) = takeRequest {
        assertRequestUrl(node) {
            addPathSegments("api/client/v2")
            addPathSegment("conferences")
            addPathSegment(conferenceAlias)
            addPathSegment("participants")
            addPathSegment(participantId)
            addPathSegment("disconnect")
        }
        assertToken(token)
        assertPostEmptyBody()
    }
}
