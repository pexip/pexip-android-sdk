package com.pexip.sdk.video.api

import com.pexip.sdk.video.api.internal.Box
import com.pexip.sdk.video.api.internal.OkHttpInfinityService
import com.pexip.sdk.video.api.internal.RequestToken403Response
import com.pexip.sdk.video.api.internal.RequestTokenRequest
import com.pexip.sdk.video.nextAlias
import com.pexip.sdk.video.nextPin
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
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class InfinityServiceTest {

    @get:Rule
    val server = MockWebServer()

    private lateinit var json: Json
    private lateinit var service: InfinityService
    private lateinit var baseUrl: HttpUrl
    private lateinit var nodeAddress: String
    private lateinit var alias: String
    private lateinit var displayName: String
    private lateinit var pin: String

    @BeforeTest
    fun setUp() {
        json = OkHttpInfinityService.Json
        service = InfinityService(OkHttpClient())
        baseUrl = server.url("/")
        nodeAddress = with(baseUrl) { "$scheme://$host:$port" }
        alias = Random.nextAlias()
        displayName = "John"
        pin = Random.nextPin()
    }

    @Test
    fun `isInMaintenanceMode throws when nodeAddress is blank`() = runBlocking<Unit> {
        assertFailsWith<IllegalArgumentException> { service.isInMaintenanceMode("   ") }
    }

    @Test
    fun `isInMaintenanceMode throws NoSuchNodeException`() = runBlocking {
        server.enqueue { setResponseCode(404) }
        assertFailsWith<NoSuchNodeException> { service.isInMaintenanceMode(nodeAddress) }
        server.verifyIsInMaintenanceMode()
    }

    @Test
    fun `isInMaintenanceMode throws IllegalStateException`() = runBlocking {
        server.enqueue { setResponseCode(500) }
        assertFailsWith<IllegalStateException> { service.isInMaintenanceMode(nodeAddress) }
        server.verifyIsInMaintenanceMode()
    }

    @Test
    fun `isInMaintenanceMode returns true`() = runBlocking {
        server.enqueue { setResponseCode(503) }
        assertTrue(service.isInMaintenanceMode(nodeAddress))
        server.verifyIsInMaintenanceMode()
    }

    @Test
    fun `isInMaintenanceMode returns false`() = runBlocking {
        server.enqueue { setResponseCode(200) }
        assertFalse(service.isInMaintenanceMode(nodeAddress))
        server.verifyIsInMaintenanceMode()
    }

    @Test
    fun `requestToken throws when any parameter is blank except pin`() = runBlocking<Unit> {
        assertFailsWith<IllegalArgumentException> {
            service.requestToken(
                nodeAddress = "   ",
                alias = alias,
                displayName = displayName,
                pin = pin
            )
        }
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
    fun `requestToken throws NoSuchNodeException`() = runBlocking {
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
    fun `requestToken throws NoSuchConferenceException`() = runBlocking {
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
    fun `requestToken throws RequiredPinException`() = runBlocking {
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
    fun `requestToken throws InvalidPinException`() = runBlocking {
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
    fun `requestToken returns Token`() = runBlocking {
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

    private fun MockWebServer.verifyIsInMaintenanceMode() = takeRequest {
        assertEquals("GET", method)
        assertEquals(baseUrl.scheme, requestUrl?.scheme)
        assertEquals(baseUrl.host, requestUrl?.host)
        assertEquals(baseUrl.port, requestUrl?.port)
        assertEquals("/api/client/v2/status", path)
    }

    private fun MockWebServer.verifyRequestToken(pin: String?) = takeRequest {
        assertEquals("POST", method)
        assertEquals(baseUrl.scheme, requestUrl?.scheme)
        assertEquals(baseUrl.host, requestUrl?.host)
        assertEquals(baseUrl.port, requestUrl?.port)
        assertEquals("/api/client/v2/conferences/$alias/request_token", path)
        assertEquals("application/json; charset=utf-8", getHeader("Content-Type"))
        assertEquals(pin?.trim(), getHeader("pin"))
        assertEquals(RequestTokenRequest(displayName), json.decodeFromBuffer(body))
    }

    @OptIn(ExperimentalSerializationApi::class)
    private inline fun <reified T> Json.decodeFromBuffer(buffer: Buffer) =
        decodeFromStream<T>(buffer.inputStream())

    private inline fun MockWebServer.enqueue(block: MockResponse.() -> Unit) =
        enqueue(MockResponse().apply(block))

    private inline fun MockWebServer.takeRequest(block: RecordedRequest.() -> Unit) =
        with(takeRequest(), block)
}
