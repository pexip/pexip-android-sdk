package com.pexip.sdk.api.infinity

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockWebServer
import org.junit.Rule
import java.net.URL
import java.util.UUID
import kotlin.random.Random
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

internal class RegistrationStepTest {

    @get:Rule
    val server = MockWebServer()

    private lateinit var node: URL
    private lateinit var registrationAlias: String
    private lateinit var username: String
    private lateinit var password: String
    private lateinit var json: Json
    private lateinit var step: InfinityService.RegistrationStep

    @BeforeTest
    fun setUp() {
        node = server.url("/").toUrl()
        registrationAlias = Random.nextString(8)
        username = Random.nextString(8)
        password = Random.nextString(8)
        json = Json { ignoreUnknownKeys = true }
        val service = InfinityService.create(OkHttpClient(), json)
        step = service.newRequest(node).registration(registrationAlias)
    }

    @Test
    fun `requestToken throws IllegalStateException`() {
        server.enqueue { setResponseCode(500) }
        assertFailsWith<IllegalStateException> { step.requestToken(username, password).execute() }
        server.verifyRequestToken()
    }

    @Test
    fun `requestToken throws NoSuchNodeException`() {
        server.enqueue { setResponseCode(404) }
        assertFailsWith<NoSuchNodeException> { step.requestToken(username, password).execute() }
        server.verifyRequestToken()
    }

    @Test
    fun `requestToken throws NoSuchRegistrationException`() {
        val message = "Unauthorized"
        server.enqueue {
            setResponseCode(401)
            setBody(message)
        }
        val e = assertFailsWith<NoSuchRegistrationException> {
            step.requestToken(username, password).execute()
        }
        assertEquals(message, e.message)
        server.verifyRequestToken()
    }

    @Test
    fun `requestToken returns`() {
        val response = RequestRegistrationTokenResponse(
            token = Random.nextString(8),
            registrationId = UUID.randomUUID(),
            expires = 120,
            directoryEnabled = Random.nextBoolean(),
            routeViaRegistrar = Random.nextBoolean()
        )
        server.enqueue {
            setResponseCode(200)
            setBody(json.encodeToString(Box(response)))
        }
        assertEquals(response, step.requestToken(username, password).execute())
        server.verifyRequestToken()
    }

    @Test
    fun `refreshToken throws IllegalStateException`() {
        server.enqueue { setResponseCode(500) }
        val token = Random.nextString(8)
        assertFailsWith<IllegalStateException> { step.refreshToken(token).execute() }
        server.verifyRefreshToken(token)
    }

    @Test
    fun `refreshToken throws NoSuchNodeException`() {
        server.enqueue { setResponseCode(404) }
        val token = Random.nextString(8)
        assertFailsWith<NoSuchNodeException> { step.refreshToken(token).execute() }
        server.verifyRefreshToken(token)
    }

    @Test
    fun `refreshToken throws NoSuchRegistrationException`() {
        val message = "Unauthorized"
        server.enqueue {
            setResponseCode(401)
            setBody(message)
        }
        val token = Random.nextString(8)
        val e = assertFailsWith<NoSuchRegistrationException> { step.refreshToken(token).execute() }
        assertEquals(message, e.message)
        server.verifyRefreshToken(token)
    }

    @Test
    fun `refreshToken throws InvalidTokenException`() {
        val message = "Invalid token"
        server.enqueue {
            setResponseCode(403)
            setBody(json.encodeToString(Box(message)))
        }
        val token = Random.nextString(8)
        val e = assertFailsWith<InvalidTokenException> { step.refreshToken(token).execute() }
        assertEquals(message, e.message)
        server.verifyRefreshToken(token)
    }

    @Test
    fun `refreshToken returns`() {
        val response = RefreshRegistrationTokenResponse(Random.nextString(8))
        server.enqueue { setBody(json.encodeToString(Box(response))) }
        val token = Random.nextString(8)
        assertEquals(response, step.refreshToken(token).execute())
        server.verifyRefreshToken(token)
    }

    @Test
    fun `releaseToken throws IllegalStateException`() {
        server.enqueue { setResponseCode(500) }
        val token = Random.nextString(8)
        assertFailsWith<IllegalStateException> { step.releaseToken(token).execute() }
        server.verifyReleaseToken(token)
    }

    @Test
    fun `releaseToken throws NoSuchNodeException`() {
        server.enqueue { setResponseCode(404) }
        val token = Random.nextString(8)
        assertFailsWith<NoSuchNodeException> { step.releaseToken(token).execute() }
        server.verifyReleaseToken(token)
    }

    @Test
    fun `releaseToken throws NoSuchRegistrationException`() {
        val message = "Unauthorized"
        server.enqueue {
            setResponseCode(401)
            setBody(message)
        }
        val token = Random.nextString(8)
        val e = assertFailsWith<NoSuchRegistrationException> { step.releaseToken(token).execute() }
        assertEquals(message, e.message)
        server.verifyReleaseToken(token)
    }

    @Test
    fun `releaseToken throws InvalidTokenException`() {
        val message = "Invalid token"
        server.enqueue {
            setResponseCode(403)
            setBody(json.encodeToString(Box(message)))
        }
        val token = Random.nextString(8)
        assertFailsWith<InvalidTokenException> { step.releaseToken(token).execute() }
        server.verifyReleaseToken(token)
    }

    @Test
    fun `releaseToken returns on 200`() {
        val result = Random.nextBoolean()
        server.enqueue {
            setResponseCode(200)
            setBody(json.encodeToString(Box(result)))
        }
        val token = Random.nextString(8)
        assertEquals(result, step.releaseToken(token).execute())
        server.verifyReleaseToken(token)
    }

    private fun MockWebServer.verifyRequestToken() = takeRequest {
        assertRequestUrl(node) {
            addPathSegments("api/client/v2")
            addPathSegment("registrations")
            addPathSegment(registrationAlias)
            addPathSegment("request_token")
        }
        assertAuthorization(username, password)
        assertPostEmptyBody()
    }

    private fun MockWebServer.verifyRefreshToken(token: String) = takeRequest {
        assertRequestUrl(node) {
            addPathSegments("api/client/v2")
            addPathSegment("registrations")
            addPathSegment(registrationAlias)
            addPathSegment("refresh_token")
        }
        assertToken(token)
        assertPostEmptyBody()
    }

    private fun MockWebServer.verifyReleaseToken(token: String) = takeRequest {
        assertRequestUrl(node) {
            addPathSegments("api/client/v2")
            addPathSegment("registrations")
            addPathSegment(registrationAlias)
            addPathSegment("release_token")
        }
        assertToken(token)
        assertPostEmptyBody()
    }
}
