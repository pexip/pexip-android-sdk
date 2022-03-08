package com.pexip.sdk.video.internal

import com.pexip.sdk.video.Box
import com.pexip.sdk.video.InvalidTokenException
import com.pexip.sdk.video.JoinDetails
import com.pexip.sdk.video.NoSuchConferenceException
import com.pexip.sdk.video.NoSuchNodeException
import com.pexip.sdk.video.Node
import com.pexip.sdk.video.decodeFromBuffer
import com.pexip.sdk.video.enqueue
import com.pexip.sdk.video.nextToken
import com.pexip.sdk.video.nextUuid
import com.pexip.sdk.video.takeRequest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import okhttp3.mockwebserver.MockWebServer
import org.junit.Rule
import kotlin.random.Random
import kotlin.random.nextInt
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.minutes

@OptIn(ExperimentalCoroutinesApi::class)
internal class InfinityServiceTest {

    @get:Rule
    val server = MockWebServer()

    private lateinit var node: Node
    private lateinit var details: JoinDetails
    private lateinit var participantId: String
    private lateinit var callId: String
    private lateinit var token: String
    private lateinit var store: TokenStore
    private lateinit var service: InfinityService

    @BeforeTest
    fun setUp() {
        node = Node(server.url("/"))
        details = JoinDetails.Builder()
            .alias("john")
            .host("example.com")
            .displayName("John")
            .build()
        participantId = Random.nextUuid()
        callId = Random.nextUuid()
        token = Random.nextToken()
        store = TokenStore(token, 2.minutes)
        service = RealInfinityService(
            client = OkHttpClient,
            store = store,
            node = node,
            joinDetails = details,
            participantId = participantId
        )
    }

    @Test
    fun `refreshToken throws IllegalStateException`() {
        server.enqueue { setResponseCode(500) }
        assertFailsWith<IllegalStateException> { service.refreshToken() }
        server.verifyRefreshToken(token)
    }

    @Test
    fun `refreshToken throws InvalidTokenException`() {
        val message = "Invalid token"
        server.enqueue {
            setResponseCode(403)
            setBody(Json.encodeToString(Box(message)))
        }
        val e = assertFailsWith<InvalidTokenException> { service.refreshToken() }
        assertEquals(message, e.message)
        server.verifyRefreshToken(token)
    }

    @Test
    fun `refreshToken throws NoSuchConferenceException`() {
        val message = "Neither conference nor gateway found"
        server.enqueue {
            setResponseCode(404)
            setBody(Json.encodeToString(Box(message)))
        }
        val e = assertFailsWith<NoSuchConferenceException> { service.refreshToken() }
        assertEquals(message, e.message)
        server.verifyRefreshToken(token)
    }

    @Test
    fun `refreshToken throws NoSuchNodeException`() {
        server.enqueue { setResponseCode(404) }
        assertFailsWith<NoSuchNodeException> { service.refreshToken() }
        server.verifyRefreshToken(token)
    }

    @Test
    fun `refreshToken returns expires`() {
        val response = RefreshToken200Response(Random.nextToken())
        server.enqueue { setBody(Json.encodeToString(Box(response))) }
        assertEquals(response.token, service.refreshToken())
        server.verifyRefreshToken(token)
    }

    @Test
    fun `refreshToken returns token`() {
        val response = RefreshToken200Response(Random.nextToken())
        server.enqueue { setBody(Json.encodeToString(Box(response))) }
        assertEquals(response.token, service.refreshToken())
        server.verifyRefreshToken(token)
    }

    @Test
    fun `releaseToken returns on non-200`() = runTest {
        server.enqueue { setResponseCode(Random.nextInt(300..599)) }
        service.releaseToken()
        server.verifyReleaseToken(token)
    }

    @Test
    fun `releaseToken returns`() = runTest {
        server.enqueue { setResponseCode(200) }
        service.releaseToken()
        server.verifyReleaseToken(token)
    }

    @Test
    fun `calls throws IllegalStateException`() {
        server.enqueue { setResponseCode(500) }
        val request = CallsRequest(Random.nextUuid())
        assertFailsWith<IllegalStateException> { service.calls(request) }
        server.verifyCalls(request)
    }

    @Test
    fun `calls returns response`() {
        val response = CallsResponse(
            call_uuid = callId,
            sdp = Random.nextUuid()
        )
        server.enqueue { setBody(Json.encodeToString(Box(response))) }
        val request = CallsRequest(Random.nextUuid())
        assertEquals(response, service.calls(request))
        server.verifyCalls(request)
    }

    @Test
    fun `ack returns on non-200`() {
        server.enqueue { setResponseCode(Random.nextInt(300..599)) }
        service.ack(AckRequest(callId))
        server.verifyAck()
    }

    @Test
    fun `ack returns`() {
        server.enqueue { setResponseCode(200) }
        service.ack(AckRequest(callId))
        server.verifyAck()
    }

    private fun MockWebServer.verifyRefreshToken(token: String) = takeRequest {
        assertEquals("POST", method)
        assertEquals(
            expected = node.address.newBuilder()
                .addPathSegments("api/client/v2/conferences")
                .addPathSegment(details.alias)
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
                .addPathSegments("api/client/v2/conferences")
                .addPathSegment(details.alias)
                .addPathSegment("release_token")
                .build(),
            actual = requestUrl
        )
        assertNull(null, getHeader("Content-Type"))
        assertEquals(token, getHeader("token"))
        assertEquals(0, body.size)
    }

    private fun MockWebServer.verifyCalls(request: CallsRequest) = takeRequest {
        assertEquals("POST", method)
        assertEquals(
            expected = node.address.newBuilder()
                .addPathSegments("api/client/v2/conferences")
                .addPathSegment(details.alias)
                .addPathSegment("participants")
                .addPathSegment(participantId)
                .addPathSegment("calls")
                .build(),
            actual = requestUrl
        )
        assertEquals("application/json; charset=utf-8", getHeader("Content-Type"))
        assertEquals(token, getHeader("token"))
        assertEquals(request, Json.decodeFromBuffer(body))
    }

    private fun MockWebServer.verifyAck() = takeRequest {
        assertEquals("POST", method)
        assertEquals(
            expected = node.address.newBuilder()
                .addPathSegments("api/client/v2/conferences")
                .addPathSegment(details.alias)
                .addPathSegment("participants")
                .addPathSegment(participantId)
                .addPathSegment("calls")
                .addPathSegment(callId)
                .addPathSegment("ack")
                .build(),
            actual = requestUrl
        )
        assertNull(null, getHeader("Content-Type"))
        assertEquals(token, getHeader("token"))
        assertEquals(0, body.size)
    }
}
