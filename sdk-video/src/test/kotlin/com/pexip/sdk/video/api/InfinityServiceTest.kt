package com.pexip.sdk.video.api

import com.pexip.sdk.video.Box
import com.pexip.sdk.video.InvalidTokenException
import com.pexip.sdk.video.NoSuchConferenceException
import com.pexip.sdk.video.NoSuchNodeException
import com.pexip.sdk.video.Token
import com.pexip.sdk.video.enqueue
import com.pexip.sdk.video.internal.Json
import com.pexip.sdk.video.nextAlias
import com.pexip.sdk.video.nextPin
import com.pexip.sdk.video.nextToken
import com.pexip.sdk.video.takeRequest
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
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.seconds

internal class InfinityServiceTest {

    @get:Rule
    val server: MockWebServer = MockWebServer()

    private lateinit var service: InfinityService
    private lateinit var nodeAddress: HttpUrl
    private lateinit var alias: String
    private lateinit var displayName: String
    private lateinit var pin: String
    private lateinit var token: String

    @BeforeTest
    fun setUp() {
        service = InfinityService(OkHttpClient())
        nodeAddress = server.url("/")
        alias = Random.nextAlias()
        displayName = "John"
        pin = Random.nextPin()
        token = Random.nextToken()
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
            setBody(Json.encodeToString(Box(message)))
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
            setBody(Json.encodeToString(Box(message)))
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
            expires = 120.seconds
        )
        server.enqueue {
            setResponseCode(200)
            setBody(Json.encodeToString(Box(newToken)))
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
            setBody(Json.encodeToString(Box(message)))
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
            setBody(Json.encodeToString(Box(message)))
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
}
