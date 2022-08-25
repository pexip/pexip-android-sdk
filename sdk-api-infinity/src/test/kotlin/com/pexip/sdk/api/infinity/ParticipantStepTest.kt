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
        val request = Random.nextCallsRequest()
        val token = Random.nextString(8)
        assertFailsWith<IllegalStateException> { step.calls(request, token).execute() }
        server.verifyCalls(request, token)
    }

    @Test
    fun `calls throws NoSuchNodeException`() {
        server.enqueue { setResponseCode(404) }
        val request = Random.nextCallsRequest()
        val token = Random.nextString(8)
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
        val token = Random.nextString(8)
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
        val token = Random.nextString(8)
        assertFailsWith<InvalidTokenException> { step.calls(request, token).execute() }
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
        val request = Random.nextCallsRequest()
        val token = Random.nextString(8)
        assertEquals(response, step.calls(request, token).execute())
        server.verifyCalls(request, token)
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

    @Test
    fun `mute throws IllegalStateException`() {
        server.enqueue { setResponseCode(500) }
        val token = Random.nextString(8)
        assertFailsWith<IllegalStateException> { step.mute(token).execute() }
        server.verifyMute(token)
    }

    @Test
    fun `mute throws NoSuchNodeException`() {
        server.enqueue { setResponseCode(404) }
        val token = Random.nextString(8)
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
        val token = Random.nextString(8)
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
        val token = Random.nextString(8)
        assertFailsWith<InvalidTokenException> { step.mute(token).execute() }
        server.verifyMute(token)
    }

    @Test
    fun `mute returns`() {
        server.enqueue { setResponseCode(200) }
        val token = Random.nextString(8)
        step.mute(token).execute()
        server.verifyMute(token)
    }

    @Test
    fun `unmute throws IllegalStateException`() {
        server.enqueue { setResponseCode(500) }
        val token = Random.nextString(8)
        assertFailsWith<IllegalStateException> { step.unmute(token).execute() }
        server.verifyUnmute(token)
    }

    @Test
    fun `unmute throws NoSuchNodeException`() {
        server.enqueue { setResponseCode(404) }
        val token = Random.nextString(8)
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
        val token = Random.nextString(8)
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
        val token = Random.nextString(8)
        assertFailsWith<InvalidTokenException> { step.unmute(token).execute() }
        server.verifyUnmute(token)
    }

    @Test
    fun `unmute returns`() {
        server.enqueue { setResponseCode(200) }
        val token = Random.nextString(8)
        step.unmute(token).execute()
        server.verifyUnmute(token)
    }

    @Test
    fun `videoMuted throws IllegalStateException`() {
        server.enqueue { setResponseCode(500) }
        val token = Random.nextString(8)
        assertFailsWith<IllegalStateException> { step.videoMuted(token).execute() }
        server.verifyVideoMuted(token)
    }

    @Test
    fun `videoMuted throws NoSuchNodeException`() {
        server.enqueue { setResponseCode(404) }
        val token = Random.nextString(8)
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
        val token = Random.nextString(8)
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
        val token = Random.nextString(8)
        assertFailsWith<InvalidTokenException> { step.videoMuted(token).execute() }
        server.verifyVideoMuted(token)
    }

    @Test
    fun `videoMuted returns`() {
        server.enqueue { setResponseCode(200) }
        val token = Random.nextString(8)
        step.videoMuted(token).execute()
        server.verifyVideoMuted(token)
    }

    @Test
    fun `videoUnmuted throws IllegalStateException`() {
        server.enqueue { setResponseCode(500) }
        val token = Random.nextString(8)
        assertFailsWith<IllegalStateException> { step.videoUnmuted(token).execute() }
        server.verifyVideoUnMuted(token)
    }

    @Test
    fun `videoUnmuted throws NoSuchNodeException`() {
        server.enqueue { setResponseCode(404) }
        val token = Random.nextString(8)
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
        val token = Random.nextString(8)
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
        val token = Random.nextString(8)
        assertFailsWith<InvalidTokenException> { step.videoUnmuted(token).execute() }
        server.verifyVideoUnMuted(token)
    }

    @Test
    fun `videoUnmuted returns`() {
        server.enqueue { setResponseCode(200) }
        val token = Random.nextString(8)
        step.videoUnmuted(token).execute()
        server.verifyVideoUnMuted(token)
    }

    @Test
    fun `takeFloor throws IllegalStateException`() {
        server.enqueue { setResponseCode(500) }
        val token = Random.nextString(8)
        assertFailsWith<IllegalStateException> { step.takeFloor(token).execute() }
        server.verifyTakeFloor(token)
    }

    @Test
    fun `takeFloor throws NoSuchNodeException`() {
        server.enqueue { setResponseCode(404) }
        val token = Random.nextString(8)
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
        val token = Random.nextString(8)
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
        val token = Random.nextString(8)
        assertFailsWith<InvalidTokenException> { step.takeFloor(token).execute() }
        server.verifyTakeFloor(token)
    }

    @Test
    fun `takeFloor returns`() {
        server.enqueue { setResponseCode(200) }
        val token = Random.nextString(8)
        step.takeFloor(token).execute()
        server.verifyTakeFloor(token)
    }

    @Test
    fun `releaseFloor throws IllegalStateException`() {
        server.enqueue { setResponseCode(500) }
        val token = Random.nextString(8)
        assertFailsWith<IllegalStateException> { step.releaseFloor(token).execute() }
        server.verifyReleaseFloor(token)
    }

    @Test
    fun `releaseFloor throws NoSuchNodeException`() {
        server.enqueue { setResponseCode(404) }
        val token = Random.nextString(8)
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
        val token = Random.nextString(8)
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
        val token = Random.nextString(8)
        assertFailsWith<InvalidTokenException> { step.releaseFloor(token).execute() }
        server.verifyReleaseFloor(token)
    }

    @Test
    fun `releaseFloor returns`() {
        server.enqueue { setResponseCode(200) }
        val token = Random.nextString(8)
        step.releaseFloor(token).execute()
        server.verifyReleaseFloor(token)
    }

    private fun MockWebServer.verifyCalls(request: CallsRequest, token: String) = takeRequest {
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

    private fun MockWebServer.verifyDtmf(request: DtmfRequest, token: String) = takeRequest {
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

    private fun MockWebServer.verifyMute(token: String) = takeRequest {
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

    private fun MockWebServer.verifyUnmute(token: String) = takeRequest {
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

    private fun MockWebServer.verifyVideoMuted(token: String) = takeRequest {
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

    private fun MockWebServer.verifyVideoUnMuted(token: String) = takeRequest {
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

    private fun MockWebServer.verifyTakeFloor(token: String) = takeRequest {
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

    private fun MockWebServer.verifyReleaseFloor(token: String) = takeRequest {
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

    private fun Random.nextCallsRequest() = CallsRequest(
        sdp = nextString(8),
        present = nextString(8),
        callType = nextString(8)
    )
}
