package com.pexip.sdk.video.internal

import com.pexip.sdk.video.Box
import com.pexip.sdk.video.InvalidTokenException
import com.pexip.sdk.video.JoinDetails
import com.pexip.sdk.video.NoSuchConferenceException
import com.pexip.sdk.video.NoSuchNodeException
import com.pexip.sdk.video.Node
import com.pexip.sdk.video.enqueue
import com.pexip.sdk.video.nextToken
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
    private lateinit var token: String
    private lateinit var service: InfinityService

    @BeforeTest
    fun setUp() {
        node = Node(server.url("/"))
        details = JoinDetails.Builder()
            .alias("john")
            .host("example.com")
            .displayName("John")
            .build()
        token = Random.nextToken()
        service = RealInfinityService(OkHttpClient, node, details, token)
    }

    @Test
    fun `refreshToken throws IllegalStateException`() = runTest {
        server.enqueue { setResponseCode(500) }
        assertFailsWith<IllegalStateException> { service.refreshToken() }
        server.verifyRefreshToken(token)
    }

    @Test
    fun `refreshToken throws InvalidTokenException`() = runTest {
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
    fun `refreshToken throws NoSuchConferenceException`() = runTest {
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
    fun `refreshToken throws NoSuchNodeException`() = runTest {
        server.enqueue { setResponseCode(404) }
        assertFailsWith<NoSuchNodeException> { service.refreshToken() }
        server.verifyRefreshToken(token)
    }

    @Test
    fun `refreshToken returns expires`() = runTest {
        val response = RequestToken200Response(Random.nextToken(), 1.minutes)
        server.enqueue { setBody(Json.encodeToString(Box(response))) }
        assertEquals(response.expires, service.refreshToken())
        server.verifyRefreshToken(token)
    }

    @Test
    fun `refreshToken updates token`() = runTest {
        val response1 = RequestToken200Response(Random.nextToken(), 1.minutes)
        server.enqueue { setBody(Json.encodeToString(Box(response1))) }
        assertEquals(response1.expires, service.refreshToken())
        server.verifyRefreshToken(token)
        val response2 = RequestToken200Response(Random.nextToken(), 2.minutes)
        server.enqueue { setBody(Json.encodeToString(Box(response2))) }
        assertEquals(response2.expires, service.refreshToken())
        server.verifyRefreshToken(response1.token)
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

    private fun MockWebServer.verifyRefreshToken(token: String) = takeRequest {
        assertEquals("POST", method)
        assertEquals(
            expected = node.address.resolve("api/client/v2/conferences/${details.alias}/refresh_token"),
            actual = requestUrl
        )
        assertNull(null, getHeader("Content-Type"))
        assertEquals(token, getHeader("token"))
        assertEquals(0, body.size)
    }

    private fun MockWebServer.verifyReleaseToken(token: String) = takeRequest {
        assertEquals("POST", method)
        assertEquals(
            expected = node.address.resolve("api/client/v2/conferences/${details.alias}/release_token"),
            actual = requestUrl
        )
        assertNull(null, getHeader("Content-Type"))
        assertEquals(token, getHeader("token"))
        assertEquals(0, body.size)
    }
}
