package com.pexip.sdk.video.api

import com.pexip.sdk.video.api.internal.Box
import com.pexip.sdk.video.api.internal.NoSuchConferenceException
import com.pexip.sdk.video.api.internal.OkHttpInfinityService
import com.pexip.sdk.video.api.internal.PinRequirementRequest
import com.pexip.sdk.video.api.internal.RequestToken200Response
import com.pexip.sdk.video.api.internal.RequestToken403Response
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import okhttp3.HttpUrl
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okio.Buffer
import org.junit.Rule
import kotlin.random.Random
import kotlin.random.nextInt
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

    @BeforeTest
    fun setUp() {
        json = OkHttpInfinityService.Json
        service = InfinityService
        baseUrl = server.url("/")
        nodeAddress = with(baseUrl) { "$scheme://$host:$port" }
        conferenceAlias = "${Random.nextInt(1000..9999)}"
        displayName = "John"
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

    private fun MockWebServer.verifyGetPinRequirement() = with(takeRequest()) {
        assertEquals("POST", method)
        assertEquals(baseUrl.scheme, requestUrl?.scheme)
        assertEquals(baseUrl.host, requestUrl?.host)
        assertEquals(baseUrl.port, requestUrl?.port)
        assertEquals("/api/client/v2/conferences/$conferenceAlias/request_token", path)
        assertEquals("application/json; charset=utf-8", getHeader("Content-Type"))
        assertEquals(PinRequirementRequest(displayName), json.decodeFromBuffer(body))
    }

    @OptIn(ExperimentalSerializationApi::class)
    private inline fun <reified T> Json.decodeFromBuffer(buffer: Buffer) =
        decodeFromStream<T>(buffer.inputStream())

    private inline fun MockWebServer.enqueue(block: MockResponse.() -> Unit) =
        enqueue(MockResponse().apply(block))
}
