package com.pexip.sdk.video.api

import com.pexip.sdk.video.api.internal.Box
import com.pexip.sdk.video.api.internal.InvalidPinException
import com.pexip.sdk.video.api.internal.NoSuchConferenceException
import com.pexip.sdk.video.api.internal.OkHttpInfinityService
import com.pexip.sdk.video.api.internal.RequestToken200Response
import com.pexip.sdk.video.api.internal.RequestToken403Response
import com.pexip.sdk.video.api.internal.RequestTokenRequest
import com.pexip.sdk.video.nextConferenceAlias
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

class InfinityServiceTest {

    @get:Rule
    val server = MockWebServer()

    private lateinit var json: Json
    private lateinit var service: InfinityService
    private lateinit var baseUrl: HttpUrl
    private lateinit var nodeAddress: String
    private lateinit var conferenceAlias: String
    private lateinit var displayName: String
    private lateinit var pin: String

    @BeforeTest
    fun setUp() {
        json = OkHttpInfinityService.Json
        service = OkHttpInfinityService(OkHttpClient())
        baseUrl = server.url("/")
        nodeAddress = with(baseUrl) { "$scheme://$host:$port" }
        conferenceAlias = Random.nextConferenceAlias()
        displayName = "John"
        pin = Random.nextPin()
    }

    @Test
    fun `getPinRequirement throws when any parameter is blank`() = runBlocking<Unit> {
        assertFailsWith<IllegalArgumentException> {
            service.getPinRequirement(
                nodeAddress = "   ",
                conferenceAlias = conferenceAlias,
                displayName = displayName
            )
        }
        assertFailsWith<IllegalArgumentException> {
            service.getPinRequirement(
                nodeAddress = nodeAddress,
                conferenceAlias = "   ",
                displayName = displayName
            )
        }
        assertFailsWith<IllegalArgumentException> {
            service.getPinRequirement(
                nodeAddress = nodeAddress,
                conferenceAlias = conferenceAlias,
                displayName = "   "
            )
        }
    }

    @Test
    fun `getPinRequirement throws NoSuchConferenceException`() = runBlocking {
        server.enqueue { setResponseCode(404) }
        assertFailsWith<NoSuchConferenceException> {
            service.getPinRequirement(
                nodeAddress = nodeAddress,
                conferenceAlias = conferenceAlias,
                displayName = displayName
            )
        }
        server.verifyGetPinRequirement()
    }

    @Test
    fun `getPinRequirement returns None`() = runBlocking {
        val response = RequestToken200Response(
            token = "${Random.nextInt()}",
            expires = 120
        )
        server.enqueue {
            setResponseCode(200)
            setBody(json.encodeToString(Box(response)))
        }
        assertEquals(
            expected = PinRequirement.None(
                token = response.token,
                expires = response.expires
            ),
            actual = service.getPinRequirement(
                nodeAddress = nodeAddress,
                conferenceAlias = conferenceAlias,
                displayName = displayName
            )
        )
        server.verifyGetPinRequirement()
    }

    @Test
    fun `getPinRequirement returns Some`() = runBlocking {
        val responses = listOf(
            RequestToken403Response("required"),
            RequestToken403Response("none")
        )
        for (response in responses) {
            server.enqueue {
                setResponseCode(403)
                setBody(json.encodeToString(Box(response)))
            }
            assertEquals(
                expected = PinRequirement.Some(response.guest_pin == "required"),
                actual = service.getPinRequirement(
                    nodeAddress = nodeAddress,
                    conferenceAlias = conferenceAlias,
                    displayName = displayName
                )
            )
            server.verifyGetPinRequirement()
        }
    }

    @Test
    fun `requestToken throws when any parameter is blank except pin`() = runBlocking<Unit> {
        assertFailsWith<IllegalArgumentException> {
            service.requestToken(
                nodeAddress = "   ",
                conferenceAlias = conferenceAlias,
                displayName = displayName,
                pin = pin
            )
        }
        assertFailsWith<IllegalArgumentException> {
            service.requestToken(
                nodeAddress = nodeAddress,
                conferenceAlias = "   ",
                displayName = displayName,
                pin = pin
            )
        }
        assertFailsWith<IllegalArgumentException> {
            service.requestToken(
                nodeAddress = nodeAddress,
                conferenceAlias = conferenceAlias,
                displayName = "   ",
                pin = pin
            )
        }
    }

    @Test
    fun `requestToken throws NoSuchConferenceException`() = runBlocking {
        server.enqueue { setResponseCode(404) }
        assertFailsWith<NoSuchConferenceException> {
            service.requestToken(
                nodeAddress = nodeAddress,
                conferenceAlias = conferenceAlias,
                displayName = displayName,
                pin = pin
            )
        }
        server.verifyRequestToken()
    }

    @Test
    fun `requestToken throws InvalidPinException`() = runBlocking {
        server.enqueue {
            setResponseCode(403)
        }
        assertFailsWith<InvalidPinException> {
            service.requestToken(
                nodeAddress = nodeAddress,
                conferenceAlias = conferenceAlias,
                displayName = displayName,
                pin = pin
            )
        }
        server.verifyRequestToken()
    }

    @Test
    fun `requestToken returns Token`() = runBlocking {
        val response = RequestToken200Response(
            token = "${Random.nextInt()}",
            expires = 120
        )
        server.enqueue {
            setResponseCode(200)
            setBody(json.encodeToString(Box(response)))
        }
        assertEquals(
            expected = Token(
                token = response.token,
                expires = response.expires
            ),
            actual = service.requestToken(
                nodeAddress = nodeAddress,
                conferenceAlias = conferenceAlias,
                displayName = displayName,
                pin = pin
            )
        )
        server.verifyRequestToken()
    }

    private fun MockWebServer.verifyGetPinRequirement() = takeRequest {
        assertEquals("POST", method)
        assertEquals(baseUrl.scheme, requestUrl?.scheme)
        assertEquals(baseUrl.host, requestUrl?.host)
        assertEquals(baseUrl.port, requestUrl?.port)
        assertEquals("/api/client/v2/conferences/$conferenceAlias/request_token", path)
        assertEquals("application/json; charset=utf-8", getHeader("Content-Type"))
        assertEquals(RequestTokenRequest(displayName), json.decodeFromBuffer(body))
    }

    private fun MockWebServer.verifyRequestToken() = takeRequest {
        assertEquals("POST", method)
        assertEquals(baseUrl.scheme, requestUrl?.scheme)
        assertEquals(baseUrl.host, requestUrl?.host)
        assertEquals(baseUrl.port, requestUrl?.port)
        assertEquals("/api/client/v2/conferences/$conferenceAlias/request_token", path)
        assertEquals("application/json; charset=utf-8", getHeader("Content-Type"))
        assertEquals(pin.trim(), getHeader("pin"))
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
