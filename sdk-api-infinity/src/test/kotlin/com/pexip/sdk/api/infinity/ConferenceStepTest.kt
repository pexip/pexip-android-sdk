/*
 * Copyright 2022-2023 Pexip AS
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

import assertk.all
import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.hasMessage
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.prop
import assertk.tableOf
import com.pexip.sdk.api.infinity.internal.RequiredPinResponse
import com.pexip.sdk.api.infinity.internal.RequiredSsoResponse
import com.pexip.sdk.api.infinity.internal.SsoRedirectResponse
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

internal class ConferenceStepTest {

    @get:Rule
    val server = MockWebServer()

    private lateinit var node: URL
    private lateinit var conferenceAlias: String
    private lateinit var json: Json
    private lateinit var step: InfinityService.ConferenceStep

    @BeforeTest
    fun setUp() {
        node = server.url("/").toUrl()
        conferenceAlias = Random.nextString(8)
        json = InfinityService.Json
        val service = InfinityService.create(OkHttpClient(), json)
        step = service.newRequest(node).conference(conferenceAlias)
    }

    @Test
    fun `requestToken throws IllegalStateException`() {
        server.enqueue { setResponseCode(500) }
        val request = RequestTokenRequest()
        assertFailure { step.requestToken(request).execute() }.isInstanceOf<IllegalStateException>()
        server.verifyRequestToken(request)
    }

    @Test
    fun `requestToken throws NoSuchNodeException`() {
        server.enqueue { setResponseCode(404) }
        val request = RequestTokenRequest()
        assertFailure { step.requestToken(request).execute() }.isInstanceOf<NoSuchNodeException>()
        server.verifyRequestToken(request)
    }

    @Test
    fun `requestToken throws NoSuchConferenceException`() {
        val message = "Neither conference nor gateway found"
        server.enqueue {
            setResponseCode(404)
            setBody(json.encodeToString(Box(message)))
        }
        val request = RequestTokenRequest(displayName = Random.nextString(16))
        assertFailure { step.requestToken(request).execute() }
            .isInstanceOf<NoSuchConferenceException>()
            .hasMessage(message)
        server.verifyRequestToken(request)
    }

    @Test
    fun `requestToken throws RequiredPinException`() {
        val responses = listOf(
            RequiredPinResponse("required"),
            RequiredPinResponse("none"),
        )
        for (response in responses) {
            server.enqueue {
                setResponseCode(403)
                setBody(json.encodeToString(Box(response)))
            }
            val request = RequestTokenRequest()
            assertFailure { step.requestToken(request).execute() }
                .isInstanceOf<RequiredPinException>()
                .prop(RequiredPinException::guestPin)
                .isEqualTo(response.guest_pin == "required")
            server.verifyRequestToken(request)
        }
    }

    @Test
    fun `requestToken throws InvalidPinException`() {
        val message = "Invalid PIN"
        server.enqueue {
            setResponseCode(403)
            setBody(json.encodeToString(Box(message)))
        }
        val request = RequestTokenRequest()
        val pin = Random.nextPin()
        assertFailure { step.requestToken(request, pin).execute() }
            .isInstanceOf<InvalidPinException>()
            .hasMessage(message)
        server.verifyRequestToken(request, pin)
    }

    @Test
    fun `requestToken throws RequiredSsoException`() {
        val idps = List(10) {
            IdentityProvider(
                name = "IdP #$it",
                id = Random.nextIdentityProviderId(),
            )
        }
        val response = RequiredSsoResponse(idps)
        server.enqueue {
            setResponseCode(403)
            setBody(json.encodeToString(Box(response)))
        }
        val request = RequestTokenRequest()
        assertFailure { step.requestToken(request).execute() }
            .isInstanceOf<RequiredSsoException>()
            .prop(RequiredSsoException::idps)
            .isEqualTo(idps)
        server.verifyRequestToken(request)
    }

    @Test
    fun `requestToken throws SsoRedirectException`() {
        val idp = IdentityProvider(
            name = "IdP #0",
            id = Random.nextIdentityProviderId(),
        )
        val response = SsoRedirectResponse(
            redirect_url = "https://example.com",
            redirect_idp = idp,
        )
        server.enqueue {
            setResponseCode(403)
            setBody(json.encodeToString(Box(response)))
        }
        val request = RequestTokenRequest(chosenIdp = idp.id)
        assertFailure { step.requestToken(request).execute() }
            .isInstanceOf<SsoRedirectException>()
            .all {
                prop(SsoRedirectException::url).isEqualTo(response.redirect_url)
                prop(SsoRedirectException::idp).isEqualTo(idp)
            }
        server.verifyRequestToken(request)
    }

    @Test
    fun `requestToken returns`() {
        tableOf("request", "pin")
            .row<_, String?>(RequestTokenRequest(directMedia = Random.nextBoolean()), null)
            .row(RequestTokenRequest(directMedia = Random.nextBoolean()), "   ")
            .row(
                val1 = RequestTokenRequest(
                    ssoToken = Random.nextString(8),
                    directMedia = false,
                ),
                val2 = Random.nextString(8),
            )
            .row(
                val1 = RequestTokenRequest(
                    ssoToken = Random.nextString(8),
                    directMedia = true,
                ),
                val2 = Random.nextString(8),
            )
            .row(
                val1 = RequestTokenRequest(
                    incomingToken = Random.nextString(8),
                    directMedia = Random.nextBoolean(),
                ),
                val2 = Random.nextString(8),
            )
            .row(
                val1 = RequestTokenRequest(
                    registrationToken = Random.nextString(8),
                    directMedia = Random.nextBoolean(),
                ),
                val2 = Random.nextString(8),
            )
            .forAll { request, pin ->
                val response = RequestTokenResponse(
                    token = Random.nextString(8),
                    conferenceName = Random.nextString(8),
                    participantId = UUID.randomUUID(),
                    participantName = Random.nextString(8),
                    expires = 120,
                    analyticsEnabled = Random.nextBoolean(),
                    chatEnabled = Random.nextBoolean(),
                    guestsCanPresent = Random.nextBoolean(),
                    serviceType = ServiceType.entries.random(),
                    version = VersionResponse(
                        versionId = Random.nextString(8),
                        pseudoVersion = Random.nextString(8),
                    ),
                    stun = List(10) {
                        StunResponse("stun:stun$it.example.com:19302")
                    },
                    turn = List(10) {
                        TurnResponse(
                            urls = listOf("turn:turn$it.example.com:3478?transport=udp"),
                            username = "${it shl 1}",
                            credential = "${it shr 1}",
                        )
                    },
                    directMedia = Random.nextBoolean(),
                    directMediaRequested = request.directMedia,
                    useRelayCandidatesOnly = Random.nextBoolean(),
                    dataChannelId = Random.nextInt(-1, 65536),
                    clientStatsUpdateInterval = Random.nextDuration(),
                )
                server.enqueue {
                    setResponseCode(200)
                    setBody(json.encodeToString(Box(response)))
                }
                val call = when (pin) {
                    null -> step.requestToken(request)
                    else -> step.requestToken(request, pin)
                }
                assertThat(call.execute(), "response").isEqualTo(response)
                server.verifyRequestToken(request, pin)
            }
    }

    @Test
    fun `refreshToken throws IllegalStateException`() {
        server.enqueue { setResponseCode(500) }
        val token = Random.nextString(8)
        assertFailure { step.refreshToken(token).execute() }.isInstanceOf<IllegalStateException>()
        server.verifyRefreshToken(token)
    }

    @Test
    fun `refreshToken throws NoSuchNodeException`() {
        server.enqueue { setResponseCode(404) }
        val token = Random.nextString(8)
        assertFailure { step.refreshToken(token).execute() }.isInstanceOf<NoSuchNodeException>()
        server.verifyRefreshToken(token)
    }

    @Test
    fun `refreshToken throws NoSuchConferenceException`() {
        val message = "Neither conference nor gateway found"
        server.enqueue {
            setResponseCode(404)
            setBody(json.encodeToString(Box(message)))
        }
        val token = Random.nextString(8)
        assertFailure { step.refreshToken(token).execute() }
            .isInstanceOf<NoSuchConferenceException>()
            .hasMessage(message)
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
        assertFailure { step.refreshToken(token).execute() }
            .isInstanceOf<InvalidTokenException>()
            .hasMessage(message)
        server.verifyRefreshToken(token)
    }

    @Test
    fun `refreshToken returns`() {
        val response = RefreshTokenResponse(
            token = Random.nextString(8),
            expires = 120,
        )
        server.enqueue { setBody(json.encodeToString(Box(response))) }
        val token = Random.nextString(8)
        assertThat(step.refreshToken(token).execute(), "response").isEqualTo(response)
        server.verifyRefreshToken(token)
    }

    @Test
    fun `releaseToken throws IllegalStateException`() {
        server.enqueue { setResponseCode(500) }
        val token = Random.nextString(8)
        assertFailure { step.releaseToken(token).execute() }.isInstanceOf<IllegalStateException>()
        server.verifyReleaseToken(token)
    }

    @Test
    fun `releaseToken throws NoSuchNodeException`() {
        server.enqueue { setResponseCode(404) }
        val token = Random.nextString(8)
        assertFailure { step.releaseToken(token).execute() }.isInstanceOf<NoSuchNodeException>()
        server.verifyReleaseToken(token)
    }

    @Test
    fun `releaseToken throws NoSuchConferenceException`() {
        val message = "Neither conference nor gateway found"
        server.enqueue {
            setResponseCode(404)
            setBody(json.encodeToString(Box(message)))
        }
        val token = Random.nextString(8)
        assertFailure { step.releaseToken(token).execute() }
            .isInstanceOf<NoSuchConferenceException>()
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
        assertFailure { step.releaseToken(token).execute() }.isInstanceOf<InvalidTokenException>()
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
        assertThat(step.releaseToken(token).execute(), "response").isEqualTo(result)
        server.verifyReleaseToken(token)
    }

    @Test
    fun `message throws IllegalStateException`() {
        server.enqueue { setResponseCode(500) }
        val token = Random.nextString(8)
        val request = Random.nextMessageRequest()
        assertFailure { step.message(request, token).execute() }
            .isInstanceOf<IllegalStateException>()
        server.verifyMessage(request, token)
    }

    @Test
    fun `message throws NoSuchNodeException`() {
        server.enqueue { setResponseCode(404) }
        val token = Random.nextString(8)
        val request = Random.nextMessageRequest()
        assertFailure { step.message(request, token).execute() }.isInstanceOf<NoSuchNodeException>()
        server.verifyMessage(request, token)
    }

    @Test
    fun `message throws NoSuchConferenceException`() {
        val message = "Neither conference nor gateway found"
        server.enqueue {
            setResponseCode(404)
            setBody(json.encodeToString(Box(message)))
        }
        val token = Random.nextString(8)
        val request = Random.nextMessageRequest()
        assertFailure { step.message(request, token).execute() }
            .isInstanceOf<NoSuchConferenceException>()
        server.verifyMessage(request, token)
    }

    @Test
    fun `message throws InvalidTokenException`() {
        val message = "Invalid token"
        server.enqueue {
            setResponseCode(403)
            setBody(json.encodeToString(Box(message)))
        }
        val token = Random.nextString(8)
        val request = Random.nextMessageRequest()
        assertFailure { step.message(request, token).execute() }
            .isInstanceOf<InvalidTokenException>()
        server.verifyMessage(request, token)
    }

    @Test
    fun `message returns result on 200`() {
        val results = listOf(true, false)
        results.forEach { result ->
            server.enqueue {
                setResponseCode(200)
                setBody(json.encodeToString(Box(result)))
            }
            val token = Random.nextString(8)
            val request = Random.nextMessageRequest()
            assertThat(step.message(request, token).execute(), "response").isEqualTo(result)
            server.verifyMessage(request, token)
        }
    }

    private fun MockWebServer.verifyRequestToken(
        request: RequestTokenRequest,
        pin: String? = null,
    ) = takeRequest {
        assertRequestUrl(node) {
            addPathSegments("api/client/v2")
            addPathSegment("conferences")
            addPathSegment(conferenceAlias)
            addPathSegment("request_token")
        }
        assertPin(pin)
        assertPost(json, request)
    }

    private fun MockWebServer.verifyRefreshToken(token: String) = takeRequest {
        assertRequestUrl(node) {
            addPathSegments("api/client/v2")
            addPathSegment("conferences")
            addPathSegment(conferenceAlias)
            addPathSegment("refresh_token")
        }
        assertToken(token)
        assertPostEmptyBody()
    }

    private fun MockWebServer.verifyReleaseToken(token: String) = takeRequest {
        assertRequestUrl(node) {
            addPathSegments("api/client/v2")
            addPathSegment("conferences")
            addPathSegment(conferenceAlias)
            addPathSegment("release_token")
        }
        assertToken(token)
        assertPostEmptyBody()
    }

    private fun MockWebServer.verifyMessage(request: MessageRequest, token: String) =
        takeRequest {
            assertRequestUrl(node) {
                addPathSegments("api/client/v2")
                addPathSegment("conferences")
                addPathSegment(conferenceAlias)
                addPathSegment("message")
            }
            assertToken(token)
            assertPost(json, request)
        }
}
