package com.pexip.sdk.video.api

import com.pexip.sdk.video.api.internal.RealInfinityService
import com.pexip.sdk.video.api.internal.RequiredPinResponse
import com.pexip.sdk.video.api.internal.RequiredSsoResponse
import com.pexip.sdk.video.api.internal.SsoRedirectResponse
import com.pexip.sdk.video.nextString
import com.pexip.sdk.video.nextToken
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockWebServer
import org.junit.Rule
import kotlin.properties.Delegates
import kotlin.random.Random
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.minutes

internal class InfinityServiceTest {

    @get:Rule
    val server = MockWebServer()

    private lateinit var json: Json
    private lateinit var service: InfinityService

    private var node: Node by Delegates.notNull()
    private var conferenceAlias: ConferenceAlias by Delegates.notNull()
    private var participantId: ParticipantId by Delegates.notNull()
    private var callId: CallId by Delegates.notNull()

    @BeforeTest
    fun setUp() {
        node = Node(server.url("/"))
        conferenceAlias = Random.nextConferenceAlias()
        participantId = Random.nextParticipantId()
        callId = Random.nextCallId()
        json = Json { ignoreUnknownKeys = true }
        service = RealInfinityService(OkHttpClient(), json)
    }

    @Test
    fun `isInMaintenanceMode throws IllegalStateException`() {
        server.enqueue { setResponseCode(500) }
        assertFailsWith<IllegalStateException> {
            service.newRequest(node)
                .status()
                .execute()
        }
        server.verifyIsInMaintenanceMode()
    }

    @Test
    fun `isInMaintenanceMode throws NoSuchNodeException`() {
        server.enqueue { setResponseCode(404) }
        assertFailsWith<NoSuchNodeException> {
            service.newRequest(node)
                .status()
                .execute()
        }
        server.verifyIsInMaintenanceMode()
    }

    @Test
    fun `isInMaintenanceMode returns true`() {
        server.enqueue { setResponseCode(503) }
        assertTrue {
            service.newRequest(node)
                .status()
                .execute()
        }
        server.verifyIsInMaintenanceMode()
    }

    @Test
    fun `isInMaintenanceMode returns false`() {
        server.enqueue { setResponseCode(200) }
        assertFalse {
            service.newRequest(node)
                .status()
                .execute()
        }
        server.verifyIsInMaintenanceMode()
    }

    @Test
    fun `requestToken throws IllegalStateException`() {
        server.enqueue { setResponseCode(500) }
        val request = RequestTokenRequest()
        assertFailsWith<IllegalStateException> {
            service.newRequest(node)
                .conference(conferenceAlias)
                .requestToken(request)
                .execute()
        }
        server.verifyRequestToken(request)
    }

    @Test
    fun `requestToken throws NoSuchNodeException`() {
        server.enqueue { setResponseCode(404) }
        val request = RequestTokenRequest()
        assertFailsWith<NoSuchNodeException> {
            service.newRequest(node)
                .conference(conferenceAlias)
                .requestToken(request)
                .execute()
        }
        server.verifyRequestToken(request)
    }

    @Test
    fun `requestToken throws NoSuchConferenceException`() {
        val message = "Neither conference nor gateway found"
        server.enqueue {
            setResponseCode(404)
            setBody(Json.encodeToString(Box(message)))
        }
        val request = RequestTokenRequest(displayName = Random.nextString(16))
        val e = assertFailsWith<NoSuchConferenceException> {
            service.newRequest(node)
                .conference(conferenceAlias)
                .requestToken(request)
                .execute()
        }
        assertEquals(message, e.message)
        server.verifyRequestToken(request)
    }

    @Test
    fun `requestToken throws RequiredPinException`() {
        val responses = listOf(
            RequiredPinResponse("required"),
            RequiredPinResponse("none")
        )
        for (response in responses) {
            server.enqueue {
                setResponseCode(403)
                setBody(Json.encodeToString(Box(response)))
            }
            val request = RequestTokenRequest()
            val e = assertFailsWith<RequiredPinException> {
                service.newRequest(node)
                    .conference(conferenceAlias)
                    .requestToken(request)
                    .execute()
            }
            assertEquals(response.guest_pin == "required", e.guestPin)
            server.verifyRequestToken(request)
        }
    }

    @Test
    fun `requestToken throws InvalidPinException`() {
        val message = "Invalid PIN"
        server.enqueue {
            setResponseCode(403)
            setBody(Json.encodeToString(Box(message)))
        }
        val request = RequestTokenRequest()
        val pin = Random.nextPin()
        val e = assertFailsWith<InvalidPinException> {
            service.newRequest(node)
                .conference(conferenceAlias)
                .requestToken(request, pin)
                .execute()
        }
        assertEquals(message, e.message)
        server.verifyRequestToken(request, pin)
    }

    @Test
    fun `requestToken throws RequiredSsoException`() {
        val idps = List(10) {
            IdentityProvider(
                name = "IdP #$it",
                id = Random.nextIdentityProviderId()
            )
        }
        val response = RequiredSsoResponse(idps)
        server.enqueue {
            setResponseCode(403)
            setBody(Json.encodeToString(Box(response)))
        }
        val request = RequestTokenRequest()
        val e = assertFailsWith<RequiredSsoException> {
            service.newRequest(node)
                .conference(conferenceAlias)
                .requestToken(request)
                .execute()
        }
        assertEquals(idps, e.idps)
        server.verifyRequestToken(request)
    }

    @Test
    fun `requestToken throws SsoRedirectException`() {
        val idp = IdentityProvider(
            name = "IdP #0",
            id = Random.nextIdentityProviderId()
        )
        val response = SsoRedirectResponse(
            redirect_url = "https://example.com",
            redirect_idp = idp
        )
        server.enqueue {
            setResponseCode(403)
            setBody(Json.encodeToString(Box(response)))
        }
        val request = RequestTokenRequest(chosenIdp = idp.id)
        val e = assertFailsWith<SsoRedirectException> {
            service.newRequest(node)
                .conference(conferenceAlias)
                .requestToken(request)
                .execute()
        }
        assertEquals(response.redirect_url, e.url)
        assertEquals(idp, e.idp)
        server.verifyRequestToken(request)
    }

    @Test
    fun `requestToken returns`() {
        val response = RequestTokenResponse(
            token = Random.nextToken(),
            participantId = Random.nextParticipantId(),
            expires = 2.minutes
        )
        server.enqueue {
            setResponseCode(200)
            setBody(Json.encodeToString(Box(response)))
        }
        val request = RequestTokenRequest(ssoToken = Random.nextSsoToken())
        assertEquals(
            expected = response,
            actual = service.newRequest(node)
                .conference(conferenceAlias)
                .requestToken(request)
                .execute()
        )
        server.verifyRequestToken(request)
    }

    @Test
    fun `refreshToken throws IllegalStateException`() {
        server.enqueue { setResponseCode(500) }
        val token = Random.nextToken()
        assertFailsWith<IllegalStateException> {
            service.newRequest(node)
                .conference(conferenceAlias)
                .refreshToken(token)
                .execute()
        }
        server.verifyRefreshToken(token)
    }

    @Test
    fun `refreshToken throws NoSuchNodeException`() {
        server.enqueue { setResponseCode(404) }
        val token = Random.nextToken()
        assertFailsWith<NoSuchNodeException> {
            service.newRequest(node)
                .conference(conferenceAlias)
                .refreshToken(token)
                .execute()
        }
        server.verifyRefreshToken(token)
    }

    @Test
    fun `refreshToken throws NoSuchConferenceException`() {
        val message = "Neither conference nor gateway found"
        server.enqueue {
            setResponseCode(404)
            setBody(Json.encodeToString(Box(message)))
        }
        val token = Random.nextToken()
        val e = assertFailsWith<NoSuchConferenceException> {
            service.newRequest(node)
                .conference(conferenceAlias)
                .refreshToken(token)
                .execute()
        }
        assertEquals(message, e.message)
        server.verifyRefreshToken(token)
    }

    @Test
    fun `refreshToken throws InvalidTokenException`() {
        val message = "Invalid token"
        server.enqueue {
            setResponseCode(403)
            setBody(Json.encodeToString(Box(message)))
        }
        val token = Random.nextToken()
        val e = assertFailsWith<InvalidTokenException> {
            service.newRequest(node)
                .conference(conferenceAlias)
                .refreshToken(token)
                .execute()
        }
        assertEquals(message, e.message)
        server.verifyRefreshToken(token)
    }

    @Test
    fun `refreshToken returns`() {
        val response = RefreshTokenResponse(Random.nextToken())
        server.enqueue { setBody(Json.encodeToString(Box(response))) }
        val token = Random.nextToken()
        assertEquals(
            expected = response,
            actual = service.newRequest(node)
                .conference(conferenceAlias)
                .refreshToken(token)
                .execute()
        )
        server.verifyRefreshToken(token)
    }

    @Test
    fun `releaseToken returns on non-200`() {
        server.enqueue { setResponseCode(500) }
        val token = Random.nextToken()
        service.newRequest(node)
            .conference(conferenceAlias)
            .releaseToken(token)
            .execute()
        server.verifyReleaseToken(token)
    }

    @Test
    fun `releaseToken returns on 200`() {
        server.enqueue { setResponseCode(200) }
        val token = Random.nextToken()
        service.newRequest(node)
            .conference(conferenceAlias)
            .releaseToken(token)
            .execute()
        server.verifyReleaseToken(token)
    }

    @Test
    fun `calls throws IllegalStateException`() {
        server.enqueue { setResponseCode(500) }
        val request = CallsRequest(sdp = Random.nextString(8))
        val token = Random.nextToken()
        assertFailsWith<IllegalStateException> {
            service.newRequest(node)
                .conference(conferenceAlias)
                .participant(participantId)
                .calls(request, token)
                .execute()
        }
        server.verifyCalls(request, token)
    }

    @Test
    fun `calls returns CallsResponse`() {
        val response = CallsResponse(
            callId = callId,
            sdp = Random.nextString(8)
        )
        server.enqueue {
            setResponseCode(200)
            setBody(json.encodeToString(Box(response)))
        }
        val request = CallsRequest(sdp = Random.nextString(8))
        val token = Random.nextToken()
        assertEquals(
            expected = response,
            actual = service.newRequest(node)
                .conference(conferenceAlias)
                .participant(participantId)
                .calls(request, token)
                .execute()
        )
        server.verifyCalls(request, token)
    }

    @Test
    fun `newCandidate returns on non-200`() {
        server.enqueue { setResponseCode(500) }
        val request = NewCandidateRequest(
            candidate = Random.nextString(8),
            mid = Random.nextString(8)
        )
        val token = Random.nextToken()
        service.newRequest(node)
            .conference(conferenceAlias)
            .participant(participantId)
            .call(callId)
            .newCandidate(request, token)
            .execute()
        server.verifyNewCandidate(request, token)
    }

    @Test
    fun `newCandidate returns on 200`() {
        server.enqueue { setResponseCode(200) }
        val request = NewCandidateRequest(
            candidate = Random.nextString(8),
            mid = Random.nextString(8)
        )
        val token = Random.nextToken()
        service.newRequest(node)
            .conference(conferenceAlias)
            .participant(participantId)
            .call(callId)
            .newCandidate(request, token)
            .execute()
        server.verifyNewCandidate(request, token)
    }

    @Test
    fun `ack returns on non-200`() {
        server.enqueue { setResponseCode(500) }
        val token = Random.nextToken()
        service.newRequest(node)
            .conference(conferenceAlias)
            .participant(participantId)
            .call(callId)
            .ack(token)
            .execute()
        server.verifyAck(token)
    }

    @Test
    fun `ack returns on 200`() {
        server.enqueue { setResponseCode(200) }
        val token = Random.nextToken()
        service.newRequest(node)
            .conference(conferenceAlias)
            .participant(participantId)
            .call(callId)
            .ack(token)
            .execute()
        server.verifyAck(token)
    }

    private fun MockWebServer.verifyIsInMaintenanceMode() = takeRequest {
        assertEquals("GET", method)
        assertEquals(
            expected = node.address.newBuilder()
                .addPathSegments("api/client/v2")
                .addPathSegment("status")
                .build(),
            actual = requestUrl
        )
    }

    private fun MockWebServer.verifyRequestToken(
        request: RequestTokenRequest,
        pin: String? = null,
    ) = takeRequest {
        assertEquals("POST", method)
        assertEquals(
            expected = node.address.newBuilder()
                .addPathSegments("api/client/v2")
                .addPathSegment("conferences")
                .addPathSegment(conferenceAlias.value)
                .addPathSegment("request_token")
                .build(),
            actual = requestUrl
        )
        assertEquals("application/json; charset=utf-8", getHeader("Content-Type"))
        assertEquals(pin?.trim(), getHeader("pin"))
        assertEquals(
            expected = request,
            actual = json.decodeFromBuffer(body)
        )
    }

    private fun MockWebServer.verifyRefreshToken(token: String) = takeRequest {
        assertEquals("POST", method)
        assertEquals(
            expected = node.address.newBuilder()
                .addPathSegments("api/client/v2")
                .addPathSegment("conferences")
                .addPathSegment(conferenceAlias.value)
                .addPathSegment("refresh_token")
                .build(),
            actual = requestUrl
        )
        assertNull(null, getHeader("Content-Type"))
        assertEquals(token, getHeader("token"))
        assertEquals(0, body.size)
    }

    private fun MockWebServer.verifyReleaseToken(token: String) = takeRequest {
        assertEquals("POST", method)
        assertEquals(
            expected = node.address.newBuilder()
                .addPathSegments("api/client/v2")
                .addPathSegment("conferences")
                .addPathSegment(conferenceAlias.value)
                .addPathSegment("release_token")
                .build(),
            actual = requestUrl
        )
        assertNull(null, getHeader("Content-Type"))
        assertEquals(token, getHeader("token"))
        assertEquals(0, body.size)
    }

    private fun MockWebServer.verifyCalls(request: CallsRequest, token: String) = takeRequest {
        assertEquals("POST", method)
        assertEquals(
            expected = node.address.newBuilder()
                .addPathSegments("api/client/v2")
                .addPathSegment("conferences")
                .addPathSegment(conferenceAlias.value)
                .addPathSegment("participants")
                .addPathSegment(participantId.value)
                .addPathSegment("calls")
                .build(),
            actual = requestUrl
        )
        assertEquals("application/json; charset=utf-8", getHeader("Content-Type"))
        assertEquals(token, getHeader("token"))
        assertEquals(request, Json.decodeFromBuffer(body))
    }

    private fun MockWebServer.verifyNewCandidate(
        request: NewCandidateRequest,
        token: String,
    ) = takeRequest {
        assertEquals("POST", method)
        assertEquals(
            expected = node.address.newBuilder()
                .addPathSegments("api/client/v2")
                .addPathSegment("conferences")
                .addPathSegment(conferenceAlias.value)
                .addPathSegment("participants")
                .addPathSegment(participantId.value)
                .addPathSegment("calls")
                .addPathSegment(callId.value)
                .addPathSegment("new_candidate")
                .build(),
            actual = requestUrl
        )
        assertEquals("application/json; charset=utf-8", getHeader("Content-Type"))
        assertEquals(token, getHeader("token"))
        assertEquals(request, Json.decodeFromBuffer(body))
    }

    private fun MockWebServer.verifyAck(token: String) = takeRequest {
        assertEquals("POST", method)
        assertEquals(
            expected = node.address.newBuilder()
                .addPathSegments("api/client/v2")
                .addPathSegment("conferences")
                .addPathSegment(conferenceAlias.value)
                .addPathSegment("participants")
                .addPathSegment(participantId.value)
                .addPathSegment("calls")
                .addPathSegment(callId.value)
                .addPathSegment("ack")
                .build(),
            actual = requestUrl
        )
        assertNull(null, getHeader("Content-Type"))
        assertEquals(token, getHeader("token"))
        assertEquals(0, body.size)
    }
}
