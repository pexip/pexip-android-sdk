package com.pexip.sdk.video.api

import com.pexip.sdk.video.api.internal.Box
import com.pexip.sdk.video.api.internal.OkHttpInfinityService
import com.pexip.sdk.video.api.internal.RequestToken403Response
import com.pexip.sdk.video.api.internal.RequestTokenRequest
import com.pexip.sdk.video.nextAlias
import com.pexip.sdk.video.nextPin
import com.pexip.sdk.video.nextToken
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import okio.Buffer
import org.junit.Rule
import kotlin.random.Random
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

internal class InfinityServiceTest {

    @get:Rule
    val server: MockWebServer = MockWebServer()

    private lateinit var json: Json
    private lateinit var service: InfinityService
    private lateinit var nodeAddress: HttpUrl
    private lateinit var alias: String
    private lateinit var displayName: String
    private lateinit var pin: String
    private lateinit var token: String

    @BeforeTest
    fun setUp() {
        json = OkHttpInfinityService.Json
        service = InfinityService(OkHttpClient())
        nodeAddress = server.url("/")
        alias = Random.nextAlias()
        displayName = "John"
        pin = Random.nextPin()
        token = Random.nextToken()
    }

    @Test
    fun `requestToken throws when any parameter is blank except pin`() = runBlocking<Unit> {
        assertFailsWith<IllegalArgumentException> {
            service.requestToken(
                nodeAddress = nodeAddress,
                alias = "   ",
                displayName = displayName,
                pin = pin
            )
        }
        assertFailsWith<IllegalArgumentException> {
            service.requestToken(
                nodeAddress = nodeAddress,
                alias = alias,
                displayName = "   ",
                pin = pin
            )
        }
    }

    @Test
    fun `requestToken throws NoSuchNodeException`(): Unit = runBlocking {
        server.enqueue {
            setResponseCode(404)
        }
        assertFailsWith<NoSuchNodeException> {
            service.requestToken(
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
            setBody(json.encodeToString(Box(message)))
        }
        val e = assertFailsWith<NoSuchConferenceException> {
            service.requestToken(
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
                setBody(json.encodeToString(Box(response)))
            }
            val e = assertFailsWith<RequiredPinException> {
                service.requestToken(
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
            setBody(json.encodeToString(Box(message)))
        }
        val e = assertFailsWith<InvalidPinException> {
            service.requestToken(
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
            setBody(json.encodeToString(Box(token)))
        }
        assertEquals(
            expected = token,
            actual = service.requestToken(
                nodeAddress = nodeAddress,
                alias = alias,
                displayName = displayName,
                pin = pin
            )
        )
        server.verifyRequestToken(pin)
    }

    @Test
    fun `refreshToken throws when any parameter is blank`() = runBlocking<Unit> {
        assertFailsWith<IllegalArgumentException> {
            service.refreshToken(
                nodeAddress = nodeAddress,
                alias = "   ",
                token = token
            )
        }
        assertFailsWith<IllegalArgumentException> {
            service.refreshToken(
                nodeAddress = nodeAddress,
                alias = alias,
                token = "   "
            )
        }
    }

    @Test
    fun `refreshToken throws NoSuchNodeException`() = runBlocking {
        server.enqueue {
            setResponseCode(404)
        }
        assertFailsWith<NoSuchNodeException> {
            service.refreshToken(
                nodeAddress = nodeAddress,
                alias = alias,
                token = token
            )
        }
        server.verifyRefreshToken()
    }

    @Test
    fun `refreshToken throws NoSuchConferenceException`() = runBlocking {
        val message = "Neither conference nor gateway found"
        server.enqueue {
            setResponseCode(404)
            setBody(json.encodeToString(Box(message)))
        }
        val e = assertFailsWith<NoSuchConferenceException> {
            service.refreshToken(
                nodeAddress = nodeAddress,
                alias = alias,
                token = token
            )
        }
        assertEquals(message, e.message)
        server.verifyRefreshToken()
    }

    @Test
    fun `refreshToken throws InvalidTokenException`() = runBlocking {
        val message = "Invalid token"
        server.enqueue {
            setResponseCode(403)
            setBody(json.encodeToString(Box(message)))
        }
        val e = assertFailsWith<InvalidTokenException> {
            service.refreshToken(
                nodeAddress = nodeAddress,
                alias = alias,
                token = token
            )
        }
        assertEquals(message, e.message)
        server.verifyRefreshToken()
    }

    @Test
    fun `refreshToken returns Token`() = runBlocking {
        val newToken = Token(
            token = "${Random.nextInt()}",
            expires = 120
        )
        server.enqueue {
            setResponseCode(200)
            setBody(json.encodeToString(Box(newToken)))
        }
        assertEquals(
            expected = newToken,
            actual = service.refreshToken(
                nodeAddress = nodeAddress,
                alias = alias,
                token = token
            )
        )
        server.verifyRefreshToken()
    }

    @Test
    fun `releaseToken throws when any parameter is blank`() = runBlocking<Unit> {
        assertFailsWith<IllegalArgumentException> {
            service.releaseToken(
                nodeAddress = nodeAddress,
                alias = "   ",
                token = token
            )
        }
        assertFailsWith<IllegalArgumentException> {
            service.releaseToken(
                nodeAddress = nodeAddress,
                alias = alias,
                token = "   "
            )
        }
    }

    @Test
    fun `releaseToken throws NoSuchNodeException`() = runBlocking {
        server.enqueue {
            setResponseCode(404)
        }
        assertFailsWith<NoSuchNodeException> {
            service.releaseToken(
                nodeAddress = nodeAddress,
                alias = alias,
                token = token
            )
        }
        server.verifyReleaseToken()
    }

    @Test
    fun `releaseToken throws NoSuchConferenceException`() = runBlocking {
        val message = "Neither conference nor gateway found"
        server.enqueue {
            setResponseCode(404)
            setBody(json.encodeToString(Box(message)))
        }
        val e = assertFailsWith<NoSuchConferenceException> {
            service.releaseToken(
                nodeAddress = nodeAddress,
                alias = alias,
                token = token
            )
        }
        assertEquals(message, e.message)
        server.verifyReleaseToken()
    }

    @Test
    fun `releaseToken returns when token is invalid`() = runBlocking {
        val message = "Invalid token"
        server.enqueue {
            setResponseCode(403)
            setBody(json.encodeToString(Box(message)))
        }
        service.releaseToken(
            nodeAddress = nodeAddress,
            alias = alias,
            token = token
        )
        server.verifyReleaseToken()
    }

    @Test
    fun `releaseToken returns`() = runBlocking {
        server.enqueue {
            setResponseCode(200)
        }
        service.releaseToken(
            nodeAddress = nodeAddress,
            alias = alias,
            token = token
        )
        server.verifyReleaseToken()
    }

    private fun MockWebServer.verifyRequestToken(pin: String?) = takeRequest {
        assertEquals("POST", method)
        assertEquals(
            expected = nodeAddress.resolve("api/client/v2/conferences/$alias/request_token"),
            actual = requestUrl
        )
        assertEquals("application/json; charset=utf-8", getHeader("Content-Type"))
        assertEquals(pin?.trim(), getHeader("pin"))
        assertEquals(RequestTokenRequest(displayName), json.decodeFromBuffer(body))
    }

    private fun MockWebServer.verifyRefreshToken() = takeRequest {
        assertEquals("POST", method)
        assertEquals(
            expected = nodeAddress.resolve("api/client/v2/conferences/$alias/refresh_token"),
            actual = requestUrl
        )
        assertNull(null, getHeader("Content-Type"))
        assertEquals(token, getHeader("token"))
        assertEquals(0, body.size)
    }

    private fun MockWebServer.verifyReleaseToken() = takeRequest {
        assertEquals("POST", method)
        assertEquals(
            expected = nodeAddress.resolve("api/client/v2/conferences/$alias/release_token"),
            actual = requestUrl
        )
        assertNull(null, getHeader("Content-Type"))
        assertEquals(token, getHeader("token"))
        assertEquals(0, body.size)
    }

    @OptIn(ExperimentalSerializationApi::class)
    private inline fun <reified T> Json.decodeFromBuffer(buffer: Buffer) =
        decodeFromStream<T>(buffer.inputStream())

    private inline fun MockWebServer.enqueue(block: MockResponse.() -> Unit) =
        enqueue(MockResponse().apply(block))

    private inline fun MockWebServer.takeRequest(block: RecordedRequest.() -> Unit) =
        with(takeRequest(), block)
}
