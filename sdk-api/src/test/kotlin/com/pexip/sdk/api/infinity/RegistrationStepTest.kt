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

import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.hasMessage
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import com.pexip.sdk.api.coroutines.await
import com.pexip.sdk.infinity.test.nextRegistrationId
import com.pexip.sdk.infinity.test.nextString
import com.pexip.sdk.infinity.test.nextVersionId
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl
import okhttp3.mockwebserver.MockWebServer
import org.junit.Rule
import kotlin.random.Random
import kotlin.random.nextInt
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds

internal class RegistrationStepTest {

    @get:Rule
    val rule = SecureMockWebServerRule()

    private val server get() = rule.server
    private val client get() = rule.client

    private lateinit var node: HttpUrl
    private lateinit var deviceAlias: String
    private lateinit var username: String
    private lateinit var password: String
    private lateinit var json: Json
    private lateinit var token: Token
    private lateinit var step: InfinityService.RegistrationStep

    @BeforeTest
    fun setUp() {
        node = server.url("/")
        deviceAlias = Random.nextString()
        username = Random.nextString()
        password = Random.nextString()
        json = InfinityService.Json
        token = Random.nextFakeToken()
        step = InfinityService.create(client, json)
            .newRequest(node)
            .registration(deviceAlias)
    }

    @Test
    fun `deviceAlias returns the correct value`() {
        assertThat(step::deviceAlias).isEqualTo(deviceAlias)
    }

    @Test
    fun `requestToken throws IllegalStateException`() = runTest {
        server.enqueue { setResponseCode(500) }
        assertFailure { step.requestToken(username, password).await() }
            .isInstanceOf<IllegalStateException>()
        server.verifyRequestToken()
    }

    @Test
    fun `requestToken throws NoSuchNodeException`() = runTest {
        server.enqueue { setResponseCode(404) }
        assertFailure { step.requestToken(username, password).await() }
            .isInstanceOf<NoSuchNodeException>()
        server.verifyRequestToken()
    }

    @Test
    fun `requestToken throws NoSuchRegistrationException`() = runTest {
        val message = "Unauthorized"
        server.enqueue {
            setResponseCode(401)
            setBody(message)
        }
        assertFailure { step.requestToken(username, password).await() }
            .isInstanceOf<NoSuchRegistrationException>()
            .hasMessage(message)
        server.verifyRequestToken()
    }

    @Test
    fun `requestToken returns`() = runTest {
        val response = RequestRegistrationTokenResponse(
            token = Random.nextString(),
            registrationId = Random.nextRegistrationId(),
            expires = Random.nextInt(10..120).seconds,
            directoryEnabled = Random.nextBoolean(),
            routeViaRegistrar = Random.nextBoolean(),
            version = VersionResponse(
                versionId = Random.nextVersionId(),
                pseudoVersion = Random.nextString(),
            ),
        )
        server.enqueue {
            setResponseCode(200)
            setBody(json.encodeToString(Box(response)))
        }
        assertThat(step.requestToken(username, password).await(), "response").isEqualTo(response)
        server.verifyRequestToken()
    }

    @Test
    fun `refreshToken throws IllegalStateException`() = runTest {
        server.enqueue { setResponseCode(500) }
        assertFailure { step.refreshToken(token).await() }.isInstanceOf<IllegalStateException>()
        server.verifyRefreshToken(token)
    }

    @Test
    fun `refreshToken throws NoSuchNodeException`() = runTest {
        server.enqueue { setResponseCode(404) }
        assertFailure { step.refreshToken(token).await() }.isInstanceOf<NoSuchNodeException>()
        server.verifyRefreshToken(token)
    }

    @Test
    fun `refreshToken throws NoSuchRegistrationException`() = runTest {
        val message = "Unauthorized"
        server.enqueue {
            setResponseCode(401)
            setBody(message)
        }
        assertFailure { step.refreshToken(token).await() }
            .isInstanceOf<NoSuchRegistrationException>()
            .hasMessage(message)
        server.verifyRefreshToken(token)
    }

    @Test
    fun `refreshToken throws InvalidTokenException`() = runTest {
        val message = "Invalid token"
        server.enqueue {
            setResponseCode(403)
            setBody(json.encodeToString(Box(message)))
        }
        assertFailure { step.refreshToken(token).await() }
            .isInstanceOf<InvalidTokenException>()
            .hasMessage(message)
        server.verifyRefreshToken(token)
    }

    @Test
    fun `refreshToken returns`() = runTest {
        val response = RefreshRegistrationTokenResponse(
            token = Random.nextString(),
            expires = Random.nextInt(10..120).seconds,
        )
        server.enqueue { setBody(json.encodeToString(Box(response))) }
        assertThat(step.refreshToken(token).await()).isEqualTo(response)
        server.verifyRefreshToken(token)
    }

    @Test
    fun `releaseToken throws IllegalStateException`() = runTest {
        server.enqueue { setResponseCode(500) }
        assertFailure { step.releaseToken(token).await() }.isInstanceOf<IllegalStateException>()
        server.verifyReleaseToken(token)
    }

    @Test
    fun `releaseToken throws NoSuchNodeException`() = runTest {
        server.enqueue { setResponseCode(404) }
        assertFailure { step.releaseToken(token).await() }.isInstanceOf<NoSuchNodeException>()
        server.verifyReleaseToken(token)
    }

    @Test
    fun `releaseToken throws NoSuchRegistrationException`() = runTest {
        val message = "Unauthorized"
        server.enqueue {
            setResponseCode(401)
            setBody(message)
        }
        assertFailure { step.releaseToken(token).await() }
            .isInstanceOf<NoSuchRegistrationException>()
            .hasMessage(message)
        server.verifyReleaseToken(token)
    }

    @Test
    fun `releaseToken throws InvalidTokenException`() = runTest {
        val message = "Invalid token"
        server.enqueue {
            setResponseCode(403)
            setBody(json.encodeToString(Box(message)))
        }
        assertFailure { step.releaseToken(token).await() }
            .isInstanceOf<InvalidTokenException>()
            .hasMessage(message)
        server.verifyReleaseToken(token)
    }

    @Test
    fun `releaseToken returns on 200`() = runTest {
        val result = Random.nextBoolean()
        server.enqueue {
            setResponseCode(200)
            setBody(json.encodeToString(Box(result)))
        }
        assertThat(step.releaseToken(token).await(), "response").isEqualTo(result)
        server.verifyReleaseToken(token)
    }

    @Test
    fun `registrations throws IllegalStateException`() = runTest {
        server.enqueue { setResponseCode(500) }
        assertFailure { step.registrations(token).await() }.isInstanceOf<IllegalStateException>()
        server.verifyRegistrations(token)
    }

    @Test
    fun `registrations throws NoSuchNodeException`() = runTest {
        server.enqueue { setResponseCode(404) }
        assertFailure { step.registrations(token).await() }.isInstanceOf<NoSuchNodeException>()
        server.verifyRegistrations(token)
    }

    @Test
    fun `registrations throws NoSuchRegistrationException`() = runTest {
        val message = "Unauthorized"
        server.enqueue {
            setResponseCode(401)
            setBody(message)
        }
        assertFailure { step.registrations(token).await() }
            .isInstanceOf<NoSuchRegistrationException>()
            .hasMessage(message)
        server.verifyRegistrations(token)
    }

    @Test
    fun `registrations throws InvalidTokenException`() = runTest {
        val message = "Invalid token"
        server.enqueue {
            setResponseCode(403)
            setBody(json.encodeToString(Box(message)))
        }
        assertFailure { step.registrations(token).await() }
            .isInstanceOf<InvalidTokenException>()
            .hasMessage(message)
        server.verifyRegistrations(token)
    }

    @Test
    fun `registrations returns a list of registrations on 200`() = runTest {
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
                setResponseCode(200)
                setBody(json.encodeToString(Box(result)))
            }
            assertThat(step.registrations(token, query).await(), "response").isEqualTo(result)
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
        assertAuthorization(username, password)
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
