package com.pexip.sdk.video

import com.pexip.sdk.video.internal.Box
import com.pexip.sdk.video.internal.Json
import com.pexip.sdk.video.internal.RequestToken403Response
import com.pexip.sdk.video.internal.RequestTokenRequest
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockWebServer
import org.junit.Rule
import kotlin.random.Random
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

internal class TokenRequesterTest {

    @get:Rule
    val server: MockWebServer = MockWebServer()

    private lateinit var nodeAddress: HttpUrl
    private lateinit var alias: String
    private lateinit var displayName: String
    private lateinit var pin: String
    private lateinit var requester: TokenRequester

    @BeforeTest
    fun setUp() {
        requester = TokenRequester.Builder()
            .client(OkHttpClient())
            .build()
        nodeAddress = server.url("/")
        alias = Random.nextAlias()
        displayName = "John"
        pin = Random.nextPin()
    }

    @Test
    fun `requestToken throws NoSuchNodeException`(): Unit = runBlocking {
        server.enqueue {
            setResponseCode(404)
        }
        assertFailsWith<NoSuchNodeException> {
            requester.requestToken(
                nodeAddress = nodeAddress,
                alias = alias,
                displayName = displayName,
                pin = pin
            )
        }
        server.verifyRequestToken(pin)
    }

    @Test
    fun `requestToken throws NoSuchConferenceException`(): Unit = runBlocking {
        val message = "Neither conference nor gateway found"
        server.enqueue {
            setResponseCode(404)
            setBody(Json.encodeToString(Box(message)))
        }
        val e = assertFailsWith<NoSuchConferenceException> {
            requester.requestToken(
                nodeAddress = nodeAddress,
                alias = alias,
                displayName = displayName,
                pin = pin
            )
        }
        assertEquals(message, e.message)
        server.verifyRequestToken(pin)
    }

    @Test
    fun `requestToken throws RequiredPinException`(): Unit = runBlocking {
        val responses = listOf(
            RequestToken403Response("required"),
            RequestToken403Response("none")
        )
        for (response in responses) {
            server.enqueue {
                setResponseCode(403)
                setBody(Json.encodeToString(Box(response)))
            }
            val e = assertFailsWith<RequiredPinException> {
                requester.requestToken(
                    nodeAddress = nodeAddress,
                    alias = alias,
                    displayName = displayName,
                    pin = null
                )
            }
            assertEquals(response.guest_pin == "required", e.guestPin)
            server.verifyRequestToken(null)
        }
    }

    @Test
    fun `requestToken throws InvalidPinException`(): Unit = runBlocking {
        val message = "Invalid PIN"
        server.enqueue {
            setResponseCode(403)
            setBody(Json.encodeToString(Box(message)))
        }
        val e = assertFailsWith<InvalidPinException> {
            requester.requestToken(
                nodeAddress = nodeAddress,
                alias = alias,
                displayName = displayName,
                pin = pin
            )
        }
        assertEquals(message, e.message)
        server.verifyRequestToken(pin)
    }

    @Test
    fun `requestToken returns Token`(): Unit = runBlocking {
        val token = Token(
            token = "${Random.nextInt()}",
            expires = 120
        )
        server.enqueue {
            setResponseCode(200)
            setBody(Json.encodeToString(Box(token)))
        }
        assertEquals(
            expected = token,
            actual = requester.requestToken(
                nodeAddress = nodeAddress,
                alias = alias,
                displayName = displayName,
                pin = pin
            )
        )
        server.verifyRequestToken(pin)
    }

    private fun MockWebServer.verifyRequestToken(pin: String?) = takeRequest {
        assertEquals("POST", method)
        assertEquals(
            expected = nodeAddress.resolve("api/client/v2/conferences/$alias/request_token"),
            actual = requestUrl
        )
        assertEquals("application/json; charset=utf-8", getHeader("Content-Type"))
        assertEquals(pin?.trim(), getHeader("pin"))
        assertEquals(
            expected = RequestTokenRequest(
                display_name = displayName,
                conference_extension = alias,
            ),
            actual = Json.decodeFromBuffer(body)
        )
    }
}
