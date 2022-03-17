package com.pexip.sdk.video.token

import com.pexip.sdk.video.Box
import com.pexip.sdk.video.decodeFromBuffer
import com.pexip.sdk.video.enqueue
import com.pexip.sdk.video.internal.HttpUrl
import com.pexip.sdk.video.internal.Json
import com.pexip.sdk.video.nextAlias
import com.pexip.sdk.video.nextPin
import com.pexip.sdk.video.nextSsoToken
import com.pexip.sdk.video.nextToken
import com.pexip.sdk.video.nextUuid
import com.pexip.sdk.video.node.Node
import com.pexip.sdk.video.takeRequest
import com.pexip.sdk.video.token.internal.RequestToken200Response
import com.pexip.sdk.video.token.internal.RequestTokenRequest
import com.pexip.sdk.video.token.internal.RequiredPinResponse
import com.pexip.sdk.video.token.internal.RequiredSsoResponse
import com.pexip.sdk.video.token.internal.SsoRedirectResponse
import kotlinx.serialization.encodeToString
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockWebServer
import org.junit.Rule
import java.util.concurrent.ExecutionException
import kotlin.random.Random
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.fail
import kotlin.time.Duration.Companion.seconds

internal class TokenRequesterTest {

    @get:Rule
    val server: MockWebServer = MockWebServer()

    private lateinit var builder: TokenRequest.Builder
    private lateinit var requester: TokenRequester

    @BeforeTest
    fun setUp() {
        val node = Node(server.url("/"))
        builder = TokenRequest.Builder()
            .alias(Random.nextAlias())
            .node(node)
            .displayName("John")
        requester = TokenRequester.Builder()
            .client(OkHttpClient())
            .build()
    }

    @Test
    fun `requestToken throws NoSuchNodeException`() {
        server.enqueue { setResponseCode(404) }
        val request = builder.build()
        val callback = object : Callback {

            override fun onFailure(requester: TokenRequester, t: Throwable) {
                throw t
            }
        }
        val e = assertFailsWith<ExecutionException> { requester.request(request, callback).get() }
        assertIs<NoSuchNodeException>(e.cause)
        server.verify(request)
    }

    @Test
    fun `requestToken throws NoSuchConferenceException`() {
        val message = "Neither conference nor gateway found"
        server.enqueue {
            setResponseCode(404)
            setBody(Json.encodeToString(Box(message)))
        }
        val request = builder.build()
        val callback = object : Callback {

            override fun onFailure(requester: TokenRequester, t: Throwable) {
                throw t
            }
        }
        val e = assertFailsWith<ExecutionException> { requester.request(request, callback).get() }
        val cause = assertIs<NoSuchConferenceException>(e.cause)
        assertEquals(message, cause.message)
        server.verify(request)
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
            val request = builder.build()
            val callback = object : Callback {

                override fun onFailure(requester: TokenRequester, t: Throwable) {
                    throw t
                }
            }
            val e = assertFailsWith<ExecutionException> {
                requester.request(request, callback).get()
            }
            val cause = assertIs<RequiredPinException>(e.cause)
            assertEquals(response.guest_pin == "required", cause.guestPin)
            server.verify(request)
        }
    }

    @Test
    fun `requestToken throws InvalidPinException`() {
        val message = "Invalid PIN"
        server.enqueue {
            setResponseCode(403)
            setBody(Json.encodeToString(Box(message)))
        }
        val request = builder
            .pin(Random.nextPin())
            .build()
        val callback = object : Callback {

            override fun onFailure(requester: TokenRequester, t: Throwable) {
                throw t
            }
        }
        val e = assertFailsWith<ExecutionException> { requester.request(request, callback).get() }
        val cause = assertIs<InvalidPinException>(e.cause)
        assertEquals(message, cause.message)
        server.verify(request)
    }

    @Test
    fun `requestToken throws RequiredSsoException`() {
        val idps = List(10) {
            IdentityProvider(
                name = "IdP #$it",
                uuid = Random.nextUuid()
            )
        }
        val response = RequiredSsoResponse(idps)
        server.enqueue {
            setResponseCode(403)
            setBody(Json.encodeToString(Box(response)))
        }
        val request = builder.build()
        val callback = object : Callback {

            override fun onFailure(requester: TokenRequester, t: Throwable) {
                throw t
            }
        }
        val e = assertFailsWith<ExecutionException> { requester.request(request, callback).get() }
        val cause = assertIs<RequiredSsoException>(e.cause)
        assertEquals(idps, cause.idps)
        server.verify(request)
    }

    @Test
    fun `requestToken throws SsoRedirectException`() {
        val idp = IdentityProvider(
            name = "IdP #0",
            uuid = Random.nextUuid()
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
        val callback = object : Callback {

            override fun onFailure(requester: TokenRequester, t: Throwable) {
                throw t
            }
        }
        val e = assertFailsWith<ExecutionException> { requester.request(request, callback).get() }
        val cause = assertIs<SsoRedirectException>(e.cause)
        assertEquals(response.redirect_url, cause.url)
        assertEquals(response.redirect_idp, cause.idp)
        server.verify(request)
    }

    @Test
    fun `requestToken returns Token`() {
        val token = RequestToken200Response(
            token = Random.nextToken(),
            participant_uuid = Random.nextUuid(),
            expires = 120.seconds
        )
        server.enqueue {
            setResponseCode(200)
            setBody(Json.encodeToString(Box(token)))
        }
        val request = builder
            .ssoToken(Random.nextSsoToken())
            .build()
        val callback = object : Callback {

            @Volatile
            var token: Token? = null

            override fun onSuccess(requester: TokenRequester, token: Token) {
                this.token = token
            }
        }
        requester.request(request, callback).get()
        assertEquals(
            expected = Token(
                address = request.conferenceAddress,
                participantId = token.participant_uuid,
                token = token.token,
                expires = token.expires
            ),
            actual = callback.token
        )
        server.verify(request)
    }

    private fun MockWebServer.verify(request: TokenRequest) = takeRequest {
        assertEquals("POST", method)
        assertEquals(
            expected = HttpUrl(request.conferenceAddress) {
                addPathSegment("request_token")
            },
            actual = requestUrl
        )
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

    private interface Callback : TokenRequester.Callback {

        override fun onSuccess(requester: TokenRequester, token: Token) {
            fail()
        }

        override fun onFailure(requester: TokenRequester, t: Throwable) {
            fail()
        }
    }
}
