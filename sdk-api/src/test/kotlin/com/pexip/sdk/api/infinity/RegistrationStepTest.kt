/*
 * Copyright 2022-2024 Pexip AS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.pexip.sdk.api.infinity

import assertk.assertions.isEqualTo
import com.pexip.sdk.infinity.test.nextRegistrationId
import com.pexip.sdk.infinity.test.nextString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import mockwebserver3.MockWebServer
import mockwebserver3.junit4.MockWebServerRule
import okhttp3.OkHttpClient
import okio.ByteString.Companion.encodeUtf8
import org.junit.Rule
import java.net.URL
import kotlin.random.Random
import kotlin.random.nextInt
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.time.Duration.Companion.seconds

internal class RegistrationStepTest {

    @get:Rule
    val rule = MockWebServerRule()

    private val server = rule.server

    private lateinit var node: URL
    private lateinit var deviceAlias: String
    private lateinit var username: String
    private lateinit var password: String
    private lateinit var json: Json
    private lateinit var token: Token
    private lateinit var step: InfinityService.RegistrationStep

    @BeforeTest
    fun setUp() {
        node = server.url("/").toUrl()
        deviceAlias = Random.nextString()
        username = Random.nextString()
        password = Random.nextString()
        json = Json { ignoreUnknownKeys = true }
        val service = InfinityService.create(OkHttpClient(), json)
        token = Random.nextFakeToken()
        step = service.newRequest(node).registration(deviceAlias)
    }

    @Test
    fun `requestToken throws IllegalStateException`() {
        server.enqueue { code(500) }
        assertFailsWith<IllegalStateException> { step.requestToken(username, password).execute() }
        server.verifyRequestToken()
    }

    @Test
    fun `requestToken throws NoSuchNodeException`() {
        server.enqueue { code(404) }
        assertFailsWith<NoSuchNodeException> { step.requestToken(username, password).execute() }
        server.verifyRequestToken()
    }

    @Test
    fun `requestToken throws NoSuchRegistrationException`() {
        val message = "Unauthorized"
        server.enqueue {
            code(401)
            body(message)
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
            token = Random.nextString(),
            registrationId = Random.nextRegistrationId(),
            expires = Random.nextInt(10..120).seconds,
            directoryEnabled = Random.nextBoolean(),
            routeViaRegistrar = Random.nextBoolean(),
            version = VersionResponse(
                versionId = Random.nextString(),
                pseudoVersion = Random.nextString(),
            ),
        )
        server.enqueue {
            code(200)
            body(json.encodeToString(Box(response)))
        }
        assertEquals(response, step.requestToken(username, password).execute())
        server.verifyRequestToken()
    }

    @Test
    fun `refreshToken throws IllegalStateException`() {
        server.enqueue { code(500) }
        assertFailsWith<IllegalStateException> { step.refreshToken(token).execute() }
        server.verifyRefreshToken(token)
    }

    @Test
    fun `refreshToken throws NoSuchNodeException`() {
        server.enqueue { code(404) }
        assertFailsWith<NoSuchNodeException> { step.refreshToken(token).execute() }
        server.verifyRefreshToken(token)
    }

    @Test
    fun `refreshToken throws NoSuchRegistrationException`() {
        val message = "Unauthorized"
        server.enqueue {
            code(401)
            body(message)
        }
        val e = assertFailsWith<NoSuchRegistrationException> { step.refreshToken(token).execute() }
        assertEquals(message, e.message)
        server.verifyRefreshToken(token)
    }

    @Test
    fun `refreshToken throws InvalidTokenException`() {
        val message = "Invalid token"
        server.enqueue {
            code(403)
            body(json.encodeToString(Box(message)))
        }
        val e = assertFailsWith<InvalidTokenException> { step.refreshToken(token).execute() }
        assertEquals(message, e.message)
        server.verifyRefreshToken(token)
    }

    @Test
    fun `refreshToken returns`() {
        val response = RefreshRegistrationTokenResponse(
            token = Random.nextString(),
            expires = Random.nextInt(10..120).seconds,
        )
        server.enqueue { body(json.encodeToString(Box(response))) }
        assertEquals(response, step.refreshToken(token).execute())
        server.verifyRefreshToken(token)
    }

    @Test
    fun `releaseToken throws IllegalStateException`() {
        server.enqueue { code(500) }
        assertFailsWith<IllegalStateException> { step.releaseToken(token).execute() }
        server.verifyReleaseToken(token)
    }

    @Test
    fun `releaseToken throws NoSuchNodeException`() {
        server.enqueue { code(404) }
        assertFailsWith<NoSuchNodeException> { step.releaseToken(token).execute() }
        server.verifyReleaseToken(token)
    }

    @Test
    fun `releaseToken throws NoSuchRegistrationException`() {
        val message = "Unauthorized"
        server.enqueue {
            code(401)
            body(message)
        }
        val e = assertFailsWith<NoSuchRegistrationException> { step.releaseToken(token).execute() }
        assertEquals(message, e.message)
        server.verifyReleaseToken(token)
    }

    @Test
    fun `releaseToken throws InvalidTokenException`() {
        val message = "Invalid token"
        server.enqueue {
            code(403)
            body(json.encodeToString(Box(message)))
        }
        assertFailsWith<InvalidTokenException> { step.releaseToken(token).execute() }
        server.verifyReleaseToken(token)
    }

    @Test
    fun `releaseToken returns on 200`() {
        val result = Random.nextBoolean()
        server.enqueue {
            code(200)
            body(json.encodeToString(Box(result)))
        }
        assertEquals(result, step.releaseToken(token).execute())
        server.verifyReleaseToken(token)
    }

    @Test
    fun `registrations throws IllegalStateException`() {
        server.enqueue { code(500) }
        assertFailsWith<IllegalStateException> { step.registrations(token).execute() }
        server.verifyRegistrations(token)
    }

    @Test
    fun `registrations throws NoSuchNodeException`() {
        server.enqueue { code(404) }
        assertFailsWith<NoSuchNodeException> { step.registrations(token).execute() }
        server.verifyRegistrations(token)
    }

    @Test
    fun `registrations throws NoSuchRegistrationException`() {
        val message = "Unauthorized"
        server.enqueue {
            code(401)
            body(message)
        }
        val e = assertFailsWith<NoSuchRegistrationException> { step.registrations(token).execute() }
        assertEquals(message, e.message)
        server.verifyRegistrations(token)
    }

    @Test
    fun `registrations throws InvalidTokenException`() {
        val message = "Invalid token"
        server.enqueue {
            code(403)
            body(json.encodeToString(Box(message)))
        }
        assertFailsWith<InvalidTokenException> { step.registrations(token).execute() }
        server.verifyRegistrations(token)
    }

    @Test
    fun `registrations returns a list of registrations on 200`() {
        val queries = List(10) { if (it == 0) "" else Random.nextString() }
        queries.forEach { query ->
            val result = List(10) {
                val username = "device$it"
                RegistrationResponse(
                    alias = "$username@example.com",
                    description = "Device #$it",
                    username = username,
                )
            }
            server.enqueue {
                code(200)
                body(json.encodeToString(Box(result)))
            }
            assertEquals(result, step.registrations(token, query).execute())
            server.verifyRegistrations(token, query)
        }
    }

    private fun MockWebServer.verifyRequestToken() = takeRequest {
        assertRequestUrl(node) {
            addPathSegments("api/client/v2")
            addPathSegment("registrations")
            addPathSegment(deviceAlias)
            addPathSegment("request_token")
        }
        val base64 = "$username:$password".encodeUtf8().base64Url()
        assertThatHeader("Authorization").isEqualTo("x-pexip-basic $base64")
        assertPostEmptyBody()
    }

    private fun MockWebServer.verifyRefreshToken(token: Token) = takeRequest {
        assertRequestUrl(node) {
            addPathSegments("api/client/v2")
            addPathSegment("registrations")
            addPathSegment(deviceAlias)
            addPathSegment("refresh_token")
        }
        assertToken(token)
        assertPostEmptyBody()
    }

    private fun MockWebServer.verifyReleaseToken(token: Token) = takeRequest {
        assertRequestUrl(node) {
            addPathSegments("api/client/v2")
            addPathSegment("registrations")
            addPathSegment(deviceAlias)
            addPathSegment("release_token")
        }
        assertToken(token)
        assertPostEmptyBody()
    }

    private fun MockWebServer.verifyRegistrations(token: Token, query: String = "") = takeRequest {
        assertRequestUrl(node) {
            addPathSegments("api/client/v2")
            addPathSegment("registrations")
            if (query.isNotBlank()) {
                addQueryParameter("q", query)
            }
        }
        assertToken(token)
        assertGet()
    }
}
