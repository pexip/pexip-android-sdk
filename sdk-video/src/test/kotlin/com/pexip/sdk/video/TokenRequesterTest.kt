package com.pexip.sdk.video

import com.pexip.sdk.video.internal.Json
import com.pexip.sdk.video.internal.RequestTokenRequest
import com.pexip.sdk.video.internal.RequiredPinResponse
import com.pexip.sdk.video.internal.RequiredSsoResponse
import com.pexip.sdk.video.internal.SsoRedirectResponse
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockWebServer
import org.junit.Rule
import java.util.UUID
import kotlin.random.Random
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
internal class TokenRequesterTest {

    @get:Rule
    val server: MockWebServer = MockWebServer()

    private lateinit var builder: TokenRequest.Builder
    private lateinit var requester: TokenRequester

    @BeforeTest
    fun setUp() {
        builder = TokenRequest.Builder()
            .nodeAddress(server.url("/"))
            .alias(Random.nextAlias())
            .displayName("John")
        requester = TokenRequester.Builder()
            .client(OkHttpClient())
            .build()
    }

    @Test
    fun `requestToken throws NoSuchNodeException`() = runTest {
        server.enqueue {
            setResponseCode(404)
        }
        val request = builder.build()
        assertFailsWith<NoSuchNodeException> { requester.requestToken(request) }
        server.verify(request)
    }

    @Test
    fun `requestToken throws NoSuchConferenceException`() = runTest {
        val message = "Neither conference nor gateway found"
        server.enqueue {
            setResponseCode(404)
            setBody(Json.encodeToString(Box(message)))
        }
        val request = builder.build()
        val e = assertFailsWith<NoSuchConferenceException> { requester.requestToken(request) }
        assertEquals(message, e.message)
        server.verify(request)
    }

    @Test
    fun `requestToken throws RequiredPinException`() = runTest {
        val responses = listOf(
            RequiredPinResponse("required"),
            RequiredPinResponse("none")
        )
        for (response in responses) {
            server.enqueue {
                setResponseCode(403)
                setBody(Json.encodeToString(Box(response)))
            }
            val request = builder.build()
            val e = assertFailsWith<RequiredPinException> { requester.requestToken(request) }
            assertEquals(response.guest_pin == "required", e.guestPin)
            server.verify(request)
        }
    }

    @Test
    fun `requestToken throws InvalidPinException`() = runTest {
        val message = "Invalid PIN"
        server.enqueue {
            setResponseCode(403)
            setBody(Json.encodeToString(Box(message)))
        }
        val request = builder
            .pin(Random.nextPin())
            .build()
        val e = assertFailsWith<InvalidPinException> { requester.requestToken(request) }
        assertEquals(message, e.message)
        server.verify(request)
    }

    @Test
    fun `requestToken throws RequiredSsoException`() = runTest {
        val idps = List(10) {
            IdentityProvider(
                name = "IdP #$it",
                uuid = UUID.randomUUID().toString()
            )
        }
        val response = RequiredSsoResponse(idps)
        server.enqueue {
            setResponseCode(403)
            setBody(Json.encodeToString(Box(response)))
        }
        val request = builder.build()
        val e = assertFailsWith<RequiredSsoException> { requester.requestToken(request) }
        assertEquals(idps, e.idps)
        server.verify(request)
    }

    @Test
    fun `requestToken throws SsoRedirectException`() = runTest {
        val idp = IdentityProvider(
            name = "IdP #0",
            uuid = UUID.randomUUID().toString()
        )
        val response = SsoRedirectResponse(
            redirect_url = "https://example.com",
            redirect_idp = idp
        )
        server.enqueue {
            setResponseCode(403)
            setBody(Json.encodeToString(Box(response)))
        }
        val request = builder
            .idp(idp)
            .build()
        val e = assertFailsWith<SsoRedirectException> { requester.requestToken(request) }
        assertEquals(e.url, response.redirect_url)
        assertEquals(e.idp, response.redirect_idp)
        server.verify(request)
    }

    @Test
    fun `requestToken returns Token`() = runTest {
        val token = Token(
            token = "${Random.nextInt()}",
            expires = 120.seconds
        )
        server.enqueue {
            setResponseCode(200)
            setBody(Json.encodeToString(Box(token)))
        }
        val request = builder
            .ssoToken(Random.nextSsoToken())
            .build()
        assertEquals(token, requester.requestToken(request))
        server.verify(request)
    }

    private fun MockWebServer.verify(request: TokenRequest) = takeRequest {
        assertEquals("POST", method)
        assertEquals(request.url, requestUrl)
        assertEquals("application/json; charset=utf-8", getHeader("Content-Type"))
        assertEquals(request.pin?.trim(), getHeader("pin"))
        assertEquals(
            expected = RequestTokenRequest(
                display_name = request.displayName,
                conference_extension = request.alias,
                chosen_idp = request.idp?.uuid,
                sso_token = request.ssoToken
            ),
            actual = Json.decodeFromBuffer(body)
        )
    }
}
