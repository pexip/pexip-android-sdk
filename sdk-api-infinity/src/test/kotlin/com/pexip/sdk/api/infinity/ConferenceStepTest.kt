package com.pexip.sdk.api.infinity

import com.pexip.sdk.api.infinity.internal.RequiredPinResponse
import com.pexip.sdk.api.infinity.internal.RequiredSsoResponse
import com.pexip.sdk.api.infinity.internal.SsoRedirectResponse
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

internal class ConferenceStepTest {

    @get:Rule
    val server = MockWebServer()

    private lateinit var node: URL
    private lateinit var conferenceAlias: String
    private lateinit var json: Json
    private lateinit var step: InfinityService.ConferenceStep

    @BeforeTest
    fun setUp() {
        node = server.url("/").toUrl()
        conferenceAlias = Random.nextString(8)
        json = Json { ignoreUnknownKeys = true }
        val service = InfinityService.create(OkHttpClient(), json)
        step = service.newRequest(node).conference(conferenceAlias)
    }

    @Test
    fun `requestToken throws IllegalStateException`() {
        server.enqueue { setResponseCode(500) }
        val request = RequestTokenRequest()
        assertFailsWith<IllegalStateException> { step.requestToken(request).execute() }
        server.verifyRequestToken(request)
    }

    @Test
    fun `requestToken throws NoSuchNodeException`() {
        server.enqueue { setResponseCode(404) }
        val request = RequestTokenRequest()
        assertFailsWith<NoSuchNodeException> { step.requestToken(request).execute() }
        server.verifyRequestToken(request)
    }

    @Test
    fun `requestToken throws NoSuchConferenceException`() {
        val message = "Neither conference nor gateway found"
        server.enqueue {
            setResponseCode(404)
            setBody(json.encodeToString(Box(message)))
        }
        val request = RequestTokenRequest(displayName = Random.nextString(16))
        val e = assertFailsWith<NoSuchConferenceException> { step.requestToken(request).execute() }
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
                setBody(json.encodeToString(Box(response)))
            }
            val request = RequestTokenRequest()
            val e = assertFailsWith<RequiredPinException> { step.requestToken(request).execute() }
            assertEquals(response.guest_pin == "required", e.guestPin)
            server.verifyRequestToken(request)
        }
    }

    @Test
    fun `requestToken throws InvalidPinException`() {
        val message = "Invalid PIN"
        server.enqueue {
            setResponseCode(403)
            setBody(json.encodeToString(Box(message)))
        }
        val request = RequestTokenRequest()
        val pin = Random.nextPin()
        val e = assertFailsWith<InvalidPinException> { step.requestToken(request, pin).execute() }
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
            setBody(json.encodeToString(Box(response)))
        }
        val request = RequestTokenRequest()
        val e = assertFailsWith<RequiredSsoException> { step.requestToken(request).execute() }
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
            setBody(json.encodeToString(Box(response)))
        }
        val request = RequestTokenRequest(chosenIdp = idp.id)
        val e = assertFailsWith<SsoRedirectException> { step.requestToken(request).execute() }
        assertEquals(response.redirect_url, e.url)
        assertEquals(idp, e.idp)
        server.verifyRequestToken(request)
    }

    @Test
    fun `requestToken returns`() {
        val response = RequestTokenResponse(
            token = Random.nextString(8),
            participantId = UUID.randomUUID(),
            participantName = Random.nextString(8),
            expires = 120,
            analyticsEnabled = Random.nextBoolean(),
            version = VersionResponse(
                versionId = Random.nextString(8),
                pseudoVersion = Random.nextString(8)
            ),
            stun = List(10) {
                StunResponse("stun:stun$it.example.com:19302")
            },
            turn = List(10) {
                TurnResponse(
                    urls = listOf("turn:turn$it.example.com:3478?transport=udp"),
                    username = "${it shl 1}",
                    credential = "${it shr 1}"
                )
            }
        )
        val requests = setOf(
            RequestTokenRequest(ssoToken = Random.nextString(8)),
            RequestTokenRequest(incomingToken = Random.nextString(8))
        )
        requests.forEach {
            server.enqueue {
                setResponseCode(200)
                setBody(json.encodeToString(Box(response)))
            }
            assertEquals(response, step.requestToken(it).execute())
            server.verifyRequestToken(it)
        }
    }

    @Test
    fun `requestToken returns if the pin is blank`() {
        val response = RequestTokenResponse(
            token = Random.nextString(8),
            participantId = UUID.randomUUID(),
            participantName = Random.nextString(8),
            expires = 120,
            analyticsEnabled = Random.nextBoolean(),
            version = VersionResponse(
                versionId = Random.nextString(8),
                pseudoVersion = Random.nextString(8)
            ),
            stun = List(10) {
                StunResponse("stun:stun$it.example.com:19302")
            },
            turn = List(10) {
                TurnResponse(
                    urls = listOf("turn:turn$it.example.com:3478?transport=udp"),
                    username = "${it shl 1}",
                    credential = "${it shr 1}"
                )
            }
        )
        server.enqueue {
            setResponseCode(200)
            setBody(json.encodeToString(Box(response)))
        }
        val request = RequestTokenRequest()
        val pin = "   "
        assertEquals(response, step.requestToken(request, pin).execute())
        server.verifyRequestToken(request, pin)
    }

    @Test
    fun `refreshToken throws IllegalStateException`() {
        server.enqueue { setResponseCode(500) }
        val token = Random.nextString(8)
        assertFailsWith<IllegalStateException> { step.refreshToken(token).execute() }
        server.verifyRefreshToken(token)
    }

    @Test
    fun `refreshToken throws NoSuchNodeException`() {
        server.enqueue { setResponseCode(404) }
        val token = Random.nextString(8)
        assertFailsWith<NoSuchNodeException> { step.refreshToken(token).execute() }
        server.verifyRefreshToken(token)
    }

    @Test
    fun `refreshToken throws NoSuchConferenceException`() {
        val message = "Neither conference nor gateway found"
        server.enqueue {
            setResponseCode(404)
            setBody(json.encodeToString(Box(message)))
        }
        val token = Random.nextString(8)
        val e = assertFailsWith<NoSuchConferenceException> { step.refreshToken(token).execute() }
        assertEquals(message, e.message)
        server.verifyRefreshToken(token)
    }

    @Test
    fun `refreshToken throws InvalidTokenException`() {
        val message = "Invalid token"
        server.enqueue {
            setResponseCode(403)
            setBody(json.encodeToString(Box(message)))
        }
        val token = Random.nextString(8)
        val e = assertFailsWith<InvalidTokenException> { step.refreshToken(token).execute() }
        assertEquals(message, e.message)
        server.verifyRefreshToken(token)
    }

    @Test
    fun `refreshToken returns`() {
        val response = RefreshTokenResponse(
            token = Random.nextString(8),
            expires = 120
        )
        server.enqueue { setBody(json.encodeToString(Box(response))) }
        val token = Random.nextString(8)
        assertEquals(response, step.refreshToken(token).execute())
        server.verifyRefreshToken(token)
    }

    @Test
    fun `releaseToken throws IllegalStateException`() {
        server.enqueue { setResponseCode(500) }
        val token = Random.nextString(8)
        assertFailsWith<IllegalStateException> { step.releaseToken(token).execute() }
        server.verifyReleaseToken(token)
    }

    @Test
    fun `releaseToken throws NoSuchNodeException`() {
        server.enqueue { setResponseCode(404) }
        val token = Random.nextString(8)
        assertFailsWith<NoSuchNodeException> { step.releaseToken(token).execute() }
        server.verifyReleaseToken(token)
    }

    @Test
    fun `releaseToken throws NoSuchConferenceException`() {
        val message = "Neither conference nor gateway found"
        server.enqueue {
            setResponseCode(404)
            setBody(json.encodeToString(Box(message)))
        }
        val token = Random.nextString(8)
        assertFailsWith<NoSuchConferenceException> { step.releaseToken(token).execute() }
        server.verifyReleaseToken(token)
    }

    @Test
    fun `releaseToken throws InvalidTokenException`() {
        val message = "Invalid token"
        server.enqueue {
            setResponseCode(403)
            setBody(json.encodeToString(Box(message)))
        }
        val token = Random.nextString(8)
        assertFailsWith<InvalidTokenException> { step.releaseToken(token).execute() }
        server.verifyReleaseToken(token)
    }

    @Test
    fun `releaseToken returns on 200`() {
        val result = Random.nextBoolean()
        server.enqueue {
            setResponseCode(200)
            setBody(json.encodeToString(Box(result)))
        }
        val token = Random.nextString(8)
        assertEquals(result, step.releaseToken(token).execute())
        server.verifyReleaseToken(token)
    }

    @Test
    fun `message throws IllegalStateException`() {
        server.enqueue { setResponseCode(500) }
        val token = Random.nextString(8)
        val request = MessageRequest(Random.nextString(8))
        assertFailsWith<IllegalStateException> { step.message(request, token).execute() }
        server.verifyMessage(request, token)
    }

    @Test
    fun `message throws NoSuchNodeException`() {
        server.enqueue { setResponseCode(404) }
        val token = Random.nextString(8)
        val request = MessageRequest(Random.nextString(8))
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
        val token = Random.nextString(8)
        val request = MessageRequest(Random.nextString(8))
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
        val token = Random.nextString(8)
        val request = MessageRequest(Random.nextString(8))
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
            val token = Random.nextString(8)
            val request = MessageRequest(Random.nextString(8))
            assertEquals(result, step.message(request, token).execute())
            server.verifyMessage(request, token)
        }
    }

    private fun MockWebServer.verifyRequestToken(
        request: RequestTokenRequest,
        pin: String? = null,
    ) = takeRequest {
        assertRequestUrl(node) {
            addPathSegments("api/client/v2")
            addPathSegment("conferences")
            addPathSegment(conferenceAlias)
            addPathSegment("request_token")
        }
        assertPin(pin)
        assertToken(request.incomingToken)
        // Copy due to incomingToken being @Transient
        assertPost(json, request.copy(incomingToken = null))
    }

    private fun MockWebServer.verifyRefreshToken(token: String) = takeRequest {
        assertRequestUrl(node) {
            addPathSegments("api/client/v2")
            addPathSegment("conferences")
            addPathSegment(conferenceAlias)
            addPathSegment("refresh_token")
        }
        assertToken(token)
        assertPostEmptyBody()
    }

    private fun MockWebServer.verifyReleaseToken(token: String) = takeRequest {
        assertRequestUrl(node) {
            addPathSegments("api/client/v2")
            addPathSegment("conferences")
            addPathSegment(conferenceAlias)
            addPathSegment("release_token")
        }
        assertToken(token)
        assertPostEmptyBody()
    }

    private fun MockWebServer.verifyMessage(request: MessageRequest, token: String) =
        takeRequest {
            assertRequestUrl(node) {
                addPathSegments("api/client/v2")
                addPathSegment("conferences")
                addPathSegment(conferenceAlias)
                addPathSegment("message")
            }
            assertToken(token)
            assertPost(json, request)
        }
}
