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

import assertk.all
import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.containsOnly
import assertk.assertions.hasMessage
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotEqualTo
import assertk.assertions.prop
import assertk.tableOf
import com.pexip.sdk.api.infinity.internal.RequiredPinResponse
import com.pexip.sdk.api.infinity.internal.RequiredSsoResponse
import com.pexip.sdk.api.infinity.internal.SsoRedirectResponse
import com.pexip.sdk.api.infinity.internal.TransformLayoutRequestSerializer
import com.pexip.sdk.api.infinity.internal.newApiClientV2Builder
import com.pexip.sdk.infinity.LayoutId
import com.pexip.sdk.infinity.ServiceType
import com.pexip.sdk.infinity.test.nextLayoutId
import com.pexip.sdk.infinity.test.nextParticipantId
import com.pexip.sdk.infinity.test.nextString
import com.pexip.sdk.infinity.test.nextVersionId
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl
import okhttp3.mockwebserver.MockWebServer
import okio.FileSystem
import org.junit.Rule
import kotlin.random.Random
import kotlin.random.nextInt
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds

internal class ConferenceStepTest {

    @get:Rule
    val rule = SecureMockWebServerRule()

    private val server get() = rule.server
    private val client get() = rule.client

    private lateinit var fileSystem: FileSystem
    private lateinit var node: HttpUrl
    private lateinit var conferenceAlias: String
    private lateinit var json: Json
    private lateinit var token: Token
    private lateinit var step: InfinityService.ConferenceStep

    @BeforeTest
    fun setUp() {
        fileSystem = FileSystem.RESOURCES
        node = server.url("/")
        conferenceAlias = Random.nextString()
        json = InfinityService.Json
        token = Random.nextFakeToken()
        step = InfinityService.create(client, json)
            .newRequest(node)
            .conference(conferenceAlias)
    }

    @Test
    fun `conferenceAlias returns the correct value`() {
        assertThat(step::conferenceAlias).isEqualTo(conferenceAlias)
    }

    @Test
    fun `conference returns a new ConferenceStep`() {
        val conferenceAlias = Random.nextString()
        assertThat(step.conference(conferenceAlias)).all {
            isNotEqualTo(step)
            prop(InfinityService.ConferenceStep::conferenceAlias).isEqualTo(conferenceAlias)
        }
    }

    @Test
    fun `requestToken throws IllegalStateException`() = runTest {
        server.enqueue { setResponseCode(500) }
        val request = RequestTokenRequest()
        assertFailure { step.requestToken(request).await() }.isInstanceOf<IllegalStateException>()
        server.verifyRequestToken(request)
    }

    @Test
    fun `requestToken throws NoSuchNodeException`() = runTest {
        server.enqueue { setResponseCode(404) }
        val request = RequestTokenRequest()
        assertFailure { step.requestToken(request).await() }.isInstanceOf<NoSuchNodeException>()
        server.verifyRequestToken(request)
    }

    @Test
    fun `requestToken throws NoSuchConferenceException`() = runTest {
        val message = "Neither conference nor gateway found"
        server.enqueue {
            setResponseCode(404)
            setBody(json.encodeToString(Box(message)))
        }
        val request = RequestTokenRequest(displayName = Random.nextString(16))
        assertFailure { step.requestToken(request).await() }
            .isInstanceOf<NoSuchConferenceException>()
            .hasMessage(message)
        server.verifyRequestToken(request)
    }

    @Test
    fun `requestToken throws RequiredPinException`() = runTest {
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
            assertFailure { step.requestToken(request).await() }
                .isInstanceOf<RequiredPinException>()
                .prop(RequiredPinException::guestPin)
                .isEqualTo(response.guest_pin == "required")
            server.verifyRequestToken(request)
        }
    }

    @Test
    fun `requestToken throws InvalidPinException`() = runTest {
        val message = "Invalid PIN"
        server.enqueue {
            setResponseCode(403)
            setBody(json.encodeToString(Box(message)))
        }
        val request = RequestTokenRequest()
        val pin = Random.nextPin()
        assertFailure { step.requestToken(request, pin).await() }
            .isInstanceOf<InvalidPinException>()
            .hasMessage(message)
        server.verifyRequestToken(request, pin)
    }

    @Test
    fun `requestToken throws RequiredSsoException`() = runTest {
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
        assertFailure { step.requestToken(request).await() }
            .isInstanceOf<RequiredSsoException>()
            .prop(RequiredSsoException::idps)
            .isEqualTo(idps)
        server.verifyRequestToken(request)
    }

    @Test
    fun `requestToken throws SsoRedirectException`() = runTest {
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
        assertFailure { step.requestToken(request).await() }
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
            .row<_, String?>(
                val1 = RequestTokenRequest(
                    directMedia = Random.nextBoolean(),
                    callTag = Random.nextString(),
                ),
                val2 = null,
            )
            .row(
                val1 = RequestTokenRequest(
                    directMedia = Random.nextBoolean(),
                    callTag = Random.nextString(),
                ),
                val2 = "   ",
            )
            .row(
                val1 = RequestTokenRequest(
                    ssoToken = Random.nextString(),
                    directMedia = false,
                    callTag = Random.nextString(),
                ),
                val2 = Random.nextString(),
            )
            .row(
                val1 = RequestTokenRequest(
                    ssoToken = Random.nextString(),
                    directMedia = true,
                    callTag = Random.nextString(),
                ),
                val2 = Random.nextString(),
            )
            .row(
                val1 = RequestTokenRequest(
                    incomingToken = Random.nextString(),
                    directMedia = Random.nextBoolean(),
                    callTag = Random.nextString(),
                ),
                val2 = Random.nextString(),
            )
            .row(
                val1 = RequestTokenRequest(
                    registrationToken = Random.nextString(),
                    directMedia = Random.nextBoolean(),
                    callTag = Random.nextString(),
                ),
                val2 = Random.nextString(),
            )
            .forAll { request, pin ->
                val response = RequestTokenResponse(
                    token = Random.nextString(),
                    conferenceName = Random.nextString(),
                    participantId = Random.nextParticipantId(),
                    participantName = Random.nextString(),
                    expires = Random.nextInt(10..120).seconds,
                    analyticsEnabled = Random.nextBoolean(),
                    chatEnabled = Random.nextBoolean(),
                    guestsCanPresent = Random.nextBoolean(),
                    serviceType = ServiceType.entries.random(),
                    version = VersionResponse(
                        versionId = Random.nextVersionId(),
                        pseudoVersion = Random.nextString(),
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
                    callTag = request.callTag,
                    parentParticipantId = Random.nextParticipantId(),
                )
                server.enqueue {
                    setResponseCode(200)
                    setBody(json.encodeToString(Box(response)))
                }
                val call = when (pin) {
                    null -> step.requestToken(request)
                    else -> step.requestToken(request, pin)
                }
                runTest { assertThat(call.await(), "response").isEqualTo(response) }
                server.verifyRequestToken(request, pin)
            }
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
    fun `refreshToken throws NoSuchConferenceException`() = runTest {
        val message = "Neither conference nor gateway found"
        server.enqueue {
            setResponseCode(404)
            setBody(json.encodeToString(Box(message)))
        }
        assertFailure { step.refreshToken(token).await() }
            .isInstanceOf<NoSuchConferenceException>()
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
        val response = RefreshTokenResponse(
            token = Random.nextString(),
            expires = Random.nextInt(10..120).seconds,
        )
        server.enqueue { setBody(json.encodeToString(Box(response))) }
        assertThat(step.refreshToken(token).await(), "response").isEqualTo(response)
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
    fun `releaseToken throws NoSuchConferenceException`() = runTest {
        val message = "Neither conference nor gateway found"
        server.enqueue {
            setResponseCode(404)
            setBody(json.encodeToString(Box(message)))
        }
        assertFailure { step.releaseToken(token).await() }
            .isInstanceOf<NoSuchConferenceException>()
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
    fun `message throws IllegalStateException`() = runTest {
        server.enqueue { setResponseCode(500) }
        val request = Random.nextMessageRequest()
        assertFailure { step.message(request, token).await() }
            .isInstanceOf<IllegalStateException>()
        server.verifyMessage(request, token)
    }

    @Test
    fun `message throws NoSuchNodeException`() = runTest {
        server.enqueue { setResponseCode(404) }
        val request = Random.nextMessageRequest()
        assertFailure { step.message(request, token).await() }.isInstanceOf<NoSuchNodeException>()
        server.verifyMessage(request, token)
    }

    @Test
    fun `message throws NoSuchConferenceException`() = runTest {
        val message = "Neither conference nor gateway found"
        server.enqueue {
            setResponseCode(404)
            setBody(json.encodeToString(Box(message)))
        }
        val request = Random.nextMessageRequest()
        assertFailure { step.message(request, token).await() }
            .isInstanceOf<NoSuchConferenceException>()
            .hasMessage(message)
        server.verifyMessage(request, token)
    }

    @Test
    fun `message throws InvalidTokenException`() = runTest {
        val message = "Invalid token"
        server.enqueue {
            setResponseCode(403)
            setBody(json.encodeToString(Box(message)))
        }
        val request = Random.nextMessageRequest()
        assertFailure { step.message(request, token).await() }
            .isInstanceOf<InvalidTokenException>()
            .hasMessage(message)
        server.verifyMessage(request, token)
    }

    @Test
    fun `message returns result on 200`() = runTest {
        val results = listOf(true, false)
        results.forEach { result ->
            server.enqueue {
                setResponseCode(200)
                setBody(json.encodeToString(Box(result)))
            }
            val request = Random.nextMessageRequest()
            assertThat(step.message(request, token).await(), "response").isEqualTo(result)
            server.verifyMessage(request, token)
        }
    }

    @Test
    fun `availableLayouts throws IllegalStateException`() = runTest {
        server.enqueue { setResponseCode(500) }
        assertFailure { step.availableLayouts(token).await() }
            .isInstanceOf<IllegalStateException>()
        server.verifyAvailableLayouts(token)
    }

    @Test
    fun `availableLayouts throws NoSuchNodeException`() = runTest {
        server.enqueue { setResponseCode(404) }
        assertFailure { step.availableLayouts(token).await() }.isInstanceOf<NoSuchNodeException>()
        server.verifyAvailableLayouts(token)
    }

    @Test
    fun `availableLayouts throws NoSuchConferenceException`() = runTest {
        val body = fileSystem.readUtf8("conference_not_found.json")
        server.enqueue {
            setResponseCode(404)
            setBody(body)
        }
        assertFailure { step.availableLayouts(token).await() }
            .isInstanceOf<NoSuchConferenceException>()
        server.verifyAvailableLayouts(token)
    }

    @Test
    fun `availableLayouts throws InvalidTokenException`() = runTest {
        val body = fileSystem.readUtf8("invalid_token.json")
        server.enqueue {
            setResponseCode(403)
            setBody(body)
        }
        assertFailure { step.availableLayouts(token).await() }
            .isInstanceOf<InvalidTokenException>()
        server.verifyAvailableLayouts(token)
    }

    @Test
    fun `availableLayouts returns a set on 200`() = runTest {
        val body = fileSystem.readUtf8("available_layouts.json")
        server.enqueue {
            setResponseCode(200)
            setBody(body)
        }
        assertThat(step.availableLayouts(token).await(), "response").containsOnly(
            LayoutId("5:7"),
            LayoutId("1:7"),
            LayoutId("2:21"),
            LayoutId("1:21"),
            LayoutId("4:0"),
            LayoutId("9:0"),
            LayoutId("16:0"),
            LayoutId("25:0"),
            LayoutId("1:0"),
            LayoutId("1:33"),
            LayoutId("teams"),
        )
        server.verifyAvailableLayouts(token)
    }

    @Test
    fun `layoutSvgs throws IllegalStateException`() = runTest {
        server.enqueue { setResponseCode(500) }
        assertFailure { step.layoutSvgs(token).await() }.isInstanceOf<IllegalStateException>()
        server.verifyLayoutSvgs(token)
    }

    @Test
    fun `layoutSvgs throws NoSuchNodeException`() = runTest {
        server.enqueue { setResponseCode(404) }
        assertFailure { step.layoutSvgs(token).await() }.isInstanceOf<NoSuchNodeException>()
        server.verifyLayoutSvgs(token)
    }

    @Test
    fun `layoutSvgs throws NoSuchConferenceException`() = runTest {
        val body = fileSystem.readUtf8("conference_not_found.json")
        server.enqueue {
            setResponseCode(404)
            setBody(body)
        }
        assertFailure { step.layoutSvgs(token).await() }.isInstanceOf<NoSuchConferenceException>()
        server.verifyLayoutSvgs(token)
    }

    @Test
    fun `layoutSvgs throws InvalidTokenException`() = runTest {
        val body = fileSystem.readUtf8("invalid_token.json")
        server.enqueue {
            setResponseCode(403)
            setBody(body)
        }
        assertFailure { step.layoutSvgs(token).await() }.isInstanceOf<InvalidTokenException>()
        server.verifyLayoutSvgs(token)
    }

    @Test
    fun `layoutSvgs returns a map on 200`() = runTest {
        val body = fileSystem.readUtf8("layout_svgs.json")
        server.enqueue {
            setResponseCode(200)
            setBody(body)
        }
        assertThat(step.layoutSvgs(token).await(), "response").containsOnly(
            LayoutId("1:7") to """<svg width='140' height='88' viewBox='0 0 140 88' fill='none' xmlns='http://www.w3.org/2000/svg'>
<rect x='0.5' y='0.5' width='139' height='87' rx='3.5' fill='currentColor' stroke='#BBBFC3'></rect>
<rect x='33' y='12.1044' width='74' height='45.3913' rx='2' fill='#BBBFC3'></rect>
<rect x='4' y='66' width='18' height='14' rx='2' fill='#BBBFC3'></rect>
<rect x='23' y='66' width='18' height='14' rx='2' fill='#BBBFC3'></rect>
<rect x='42' y='66' width='18' height='14' rx='2' fill='#BBBFC3'></rect>
<rect x='61' y='66' width='18' height='14' rx='2' fill='#BBBFC3'></rect>
<rect x='80' y='66' width='18' height='14' rx='2' fill='#BBBFC3'></rect>
<rect x='99' y='66' width='18' height='14' rx='2' fill='#BBBFC3'></rect>
<rect x='118' y='66' width='18' height='14' rx='2' fill='#BBBFC3'></rect></svg>""",
        )
        server.verifyLayoutSvgs(token)
    }

    @Test
    fun `transformLayout throws IllegalStateException`() = runTest {
        server.enqueue { setResponseCode(500) }
        val request = TransformLayoutRequest(
            layout = Random.nextLayoutId(),
            guestLayout = Random.nextLayoutId(),
            enableOverlayText = Random.nextBoolean(),
        )
        assertFailure { step.transformLayout(request, token).await() }
            .isInstanceOf<IllegalStateException>()
        server.verifyTransformLayout(request, token)
    }

    @Test
    fun `transformLayout throws IllegalLayoutTransformException`() = runTest {
        val body = fileSystem.readUtf8("illegal_layout_transform.json")
        server.enqueue {
            setResponseCode(400)
            setBody(body)
        }
        val request = TransformLayoutRequest(
            layout = Random.nextLayoutId(),
            guestLayout = Random.nextLayoutId(),
            enableOverlayText = Random.nextBoolean(),
        )
        assertFailure { step.transformLayout(request, token).await() }
            .isInstanceOf<IllegalLayoutTransformException>()
        server.verifyTransformLayout(request, token)
    }

    @Test
    fun `transformLayout throws NoSuchNodeException`() = runTest {
        server.enqueue { setResponseCode(404) }
        val request = TransformLayoutRequest(
            layout = Random.nextLayoutId(),
            guestLayout = Random.nextLayoutId(),
            enableOverlayText = Random.nextBoolean(),
        )
        assertFailure { step.transformLayout(request, token).await() }
            .isInstanceOf<NoSuchNodeException>()
        server.verifyTransformLayout(request, token)
    }

    @Test
    fun `transformLayout throws NoSuchConferenceException`() = runTest {
        val body = fileSystem.readUtf8("conference_not_found.json")
        server.enqueue {
            setResponseCode(404)
            setBody(body)
        }
        val request = TransformLayoutRequest(
            layout = Random.nextLayoutId(),
            guestLayout = Random.nextLayoutId(),
            enableOverlayText = Random.nextBoolean(),
        )
        assertFailure { step.transformLayout(request, token).await() }
            .isInstanceOf<NoSuchConferenceException>()
        server.verifyTransformLayout(request, token)
    }

    @Test
    fun `transformLayout throws InvalidTokenException`() = runTest {
        val body = fileSystem.readUtf8("invalid_token.json")
        server.enqueue {
            setResponseCode(403)
            setBody(body)
        }
        val request = TransformLayoutRequest(
            layout = Random.nextLayoutId(),
            guestLayout = Random.nextLayoutId(),
            enableOverlayText = Random.nextBoolean(),
        )
        assertFailure { step.transformLayout(request, token).await() }
            .isInstanceOf<InvalidTokenException>()
        server.verifyTransformLayout(request, token)
    }

    @Test
    fun `transformLayout returns result on 200`() = runTest {
        val results = listOf(true, false)
        results.forEach { result ->
            server.enqueue {
                setResponseCode(200)
                setBody(json.encodeToString(Box(result)))
            }
            val request = TransformLayoutRequest(
                layout = Random.nextLayoutId(),
                guestLayout = Random.nextLayoutId(),
                enableOverlayText = Random.nextBoolean(),
            )
            assertThat(step.transformLayout(request, token).await(), "response").isEqualTo(result)
            server.verifyTransformLayout(request, token)
        }
    }

    @Test
    fun `theme throws IllegalStateException`() = runTest {
        server.enqueue { setResponseCode(500) }
        assertFailure { step.theme(token).await() }.isInstanceOf<IllegalStateException>()
        server.verifyTheme(token)
    }

    @Test
    fun `theme throws NoSuchNodeException`() = runTest {
        server.enqueue { setResponseCode(404) }
        assertFailure { step.theme(token).await() }.isInstanceOf<NoSuchNodeException>()
        server.verifyTheme(token)
    }

    @Test
    fun `theme throws NoSuchConferenceException`() = runTest {
        val message = "Neither conference nor gateway found"
        server.enqueue {
            setResponseCode(404)
            setBody(json.encodeToString(Box(message)))
        }
        assertFailure { step.theme(token).await() }
            .isInstanceOf<NoSuchConferenceException>()
            .hasMessage(message)
        server.verifyTheme(token)
    }

    @Test
    fun `theme throws InvalidTokenException`() = runTest {
        val message = "Invalid token"
        server.enqueue {
            setResponseCode(403)
            setBody(json.encodeToString(Box(message)))
        }
        assertFailure { step.theme(token).await() }
            .isInstanceOf<InvalidTokenException>()
            .hasMessage(message)
        server.verifyTheme(token)
    }

    @Test
    fun `theme returns empty map on 204`() = runTest {
        server.enqueue { setResponseCode(204) }
        assertThat(step.theme(token).await(), "response").isEmpty()
        server.verifyTheme(token)
    }

    @Test
    fun `theme returns a map on 200`() = runTest {
        val map = mapOf(
            Random.nextString() to SplashScreenResponse(
                background = BackgroundResponse("background.jpg"),
                elements = List(10) {
                    ElementResponse.Text(
                        color = Random.nextLong(Long.MAX_VALUE),
                        text = Random.nextString(),
                    )
                },
            ),
        )
        server.enqueue {
            setResponseCode(200)
            setBody(json.encodeToString(Box(map)))
        }
        assertThat(step.theme(token).await(), "response").isEqualTo(map)
        server.verifyTheme(token)
    }

    @Test
    fun `theme with path returns correct URL with token`() {
        val path = Random.nextString()
        val actual = step.theme(path, token)
        val expected = node.newApiClientV2Builder()
            .addPathSegment("conferences")
            .addPathSegment(conferenceAlias)
            .addPathSegment("theme")
            .addPathSegment(path)
            .addQueryParameter("token", token.token)
            .build()
        assertThat(actual).isEqualTo(expected.toString())
    }

    @Test
    fun `clearAllBuzz throws IllegalStateException`() = runTest {
        server.enqueue { setResponseCode(500) }
        assertFailure { step.clearAllBuzz(token).await() }.isInstanceOf<IllegalStateException>()
        server.verifyClearAllBuzz(token)
    }

    @Test
    fun `clearAllBuzz throws NoSuchNodeException`() = runTest {
        server.enqueue { setResponseCode(404) }
        assertFailure { step.clearAllBuzz(token).await() }.isInstanceOf<NoSuchNodeException>()
        server.verifyClearAllBuzz(token)
    }

    @Test
    fun `clearAllBuzz throws NoSuchConferenceException`() = runTest {
        val message = "Neither conference nor gateway found"
        server.enqueue {
            setResponseCode(404)
            setBody(json.encodeToString(Box(message)))
        }
        assertFailure { step.clearAllBuzz(token).await() }
            .isInstanceOf<NoSuchConferenceException>()
            .hasMessage(message)
        server.verifyClearAllBuzz(token)
    }

    @Test
    fun `clearAllBuzz throws InvalidTokenException`() = runTest {
        val message = "Invalid token"
        server.enqueue {
            setResponseCode(403)
            setBody(json.encodeToString(Box(message)))
        }
        assertFailure { step.clearAllBuzz(token).await() }
            .isInstanceOf<InvalidTokenException>()
            .hasMessage(message)
        server.verifyClearAllBuzz(token)
    }

    @Test
    fun `clearAllBuzz returns result on 200`() = runTest {
        val results = listOf(true, false)
        results.forEach { result ->
            server.enqueue {
                setResponseCode(200)
                setBody(json.encodeToString(Box(result)))
            }
            assertThat(step.clearAllBuzz(token).await(), "result").isEqualTo(result)
            server.verifyClearAllBuzz(token)
        }
    }

    @Test
    fun `lock throws IllegalStateException`() = runTest {
        server.enqueue { setResponseCode(500) }
        assertFailure { step.lock(token).await() }.isInstanceOf<IllegalStateException>()
        server.verifyLock(token)
    }

    @Test
    fun `lock throws NoSuchNodeException`() = runTest {
        server.enqueue { setResponseCode(404) }
        assertFailure { step.lock(token).await() }.isInstanceOf<NoSuchNodeException>()
        server.verifyLock(token)
    }

    @Test
    fun `lock throws NoSuchConferenceException`() = runTest {
        val message = "Neither conference nor gateway found"
        server.enqueue {
            setResponseCode(404)
            setBody(json.encodeToString(Box(message)))
        }
        assertFailure { step.lock(token).await() }
            .isInstanceOf<NoSuchConferenceException>()
            .hasMessage(message)
        server.verifyLock(token)
    }

    @Test
    fun `lock throws InvalidTokenException`() = runTest {
        val message = "Invalid token"
        server.enqueue {
            setResponseCode(403)
            setBody(json.encodeToString(Box(message)))
        }
        assertFailure { step.lock(token).await() }
            .isInstanceOf<InvalidTokenException>()
            .hasMessage(message)
        server.verifyLock(token)
    }

    @Test
    fun `lock returns result on 200`() = runTest {
        val results = listOf(true, false)
        results.forEach { result ->
            server.enqueue {
                setResponseCode(200)
                setBody(json.encodeToString(Box(result)))
            }
            assertThat(step.lock(token).await(), "result").isEqualTo(result)
            server.verifyLock(token)
        }
    }

    @Test
    fun `unlock throws IllegalStateException`() = runTest {
        server.enqueue { setResponseCode(500) }
        assertFailure { step.unlock(token).await() }.isInstanceOf<IllegalStateException>()
        server.verifyUnlock(token)
    }

    @Test
    fun `unlock throws NoSuchNodeException`() = runTest {
        server.enqueue { setResponseCode(404) }
        assertFailure { step.unlock(token).await() }.isInstanceOf<NoSuchNodeException>()
        server.verifyUnlock(token)
    }

    @Test
    fun `unlock throws NoSuchConferenceException`() = runTest {
        val message = "Neither conference nor gateway found"
        server.enqueue {
            setResponseCode(404)
            setBody(json.encodeToString(Box(message)))
        }
        assertFailure { step.unlock(token).await() }
            .isInstanceOf<NoSuchConferenceException>()
            .hasMessage(message)
        server.verifyUnlock(token)
    }

    @Test
    fun `unlock throws InvalidTokenException`() = runTest {
        val message = "Invalid token"
        server.enqueue {
            setResponseCode(403)
            setBody(json.encodeToString(Box(message)))
        }
        assertFailure { step.unlock(token).await() }
            .isInstanceOf<InvalidTokenException>()
            .hasMessage(message)
        server.verifyUnlock(token)
    }

    @Test
    fun `unlock returns result on 200`() = runTest {
        val results = listOf(true, false)
        results.forEach { result ->
            server.enqueue {
                setResponseCode(200)
                setBody(json.encodeToString(Box(result)))
            }
            assertThat(step.unlock(token).await(), "result").isEqualTo(result)
            server.verifyUnlock(token)
        }
    }

    @Test
    fun `muteGuests throws IllegalStateException`() = runTest {
        server.enqueue { setResponseCode(500) }
        assertFailure { step.muteGuests(token).await() }.isInstanceOf<IllegalStateException>()
        server.verifyMuteGuests(token)
    }

    @Test
    fun `muteGuests throws NoSuchNodeException`() = runTest {
        server.enqueue { setResponseCode(404) }
        assertFailure { step.muteGuests(token).await() }.isInstanceOf<NoSuchNodeException>()
        server.verifyMuteGuests(token)
    }

    @Test
    fun `muteGuests throws NoSuchConferenceException`() = runTest {
        val message = "Neither conference nor gateway found"
        server.enqueue {
            setResponseCode(404)
            setBody(json.encodeToString(Box(message)))
        }
        assertFailure { step.muteGuests(token).await() }
            .isInstanceOf<NoSuchConferenceException>()
            .hasMessage(message)
        server.verifyMuteGuests(token)
    }

    @Test
    fun `muteGuests throws InvalidTokenException`() = runTest {
        val message = "Invalid token"
        server.enqueue {
            setResponseCode(403)
            setBody(json.encodeToString(Box(message)))
        }
        assertFailure { step.muteGuests(token).await() }
            .isInstanceOf<InvalidTokenException>()
            .hasMessage(message)
        server.verifyMuteGuests(token)
    }

    @Test
    fun `muteGuests returns result on 200`() = runTest {
        val results = listOf(true, false)
        results.forEach { result ->
            server.enqueue {
                setResponseCode(200)
                setBody(json.encodeToString(Box(result)))
            }
            assertThat(step.muteGuests(token).await(), "result").isEqualTo(result)
            server.verifyMuteGuests(token)
        }
    }

    @Test
    fun `unmuteGuests throws IllegalStateException`() = runTest {
        server.enqueue { setResponseCode(500) }
        assertFailure { step.unmuteGuests(token).await() }.isInstanceOf<IllegalStateException>()
        server.verifyUnmuteGuests(token)
    }

    @Test
    fun `unmuteGuests throws NoSuchNodeException`() = runTest {
        server.enqueue { setResponseCode(404) }
        assertFailure { step.unmuteGuests(token).await() }.isInstanceOf<NoSuchNodeException>()
        server.verifyUnmuteGuests(token)
    }

    @Test
    fun `unmuteGuests throws NoSuchConferenceException`() = runTest {
        val message = "Neither conference nor gateway found"
        server.enqueue {
            setResponseCode(404)
            setBody(json.encodeToString(Box(message)))
        }
        assertFailure { step.unmuteGuests(token).await() }
            .isInstanceOf<NoSuchConferenceException>()
            .hasMessage(message)
        server.verifyUnmuteGuests(token)
    }

    @Test
    fun `unmuteGuests throws InvalidTokenException`() = runTest {
        val message = "Invalid token"
        server.enqueue {
            setResponseCode(403)
            setBody(json.encodeToString(Box(message)))
        }
        assertFailure { step.unmuteGuests(token).await() }
            .isInstanceOf<InvalidTokenException>()
            .hasMessage(message)
        server.verifyUnmuteGuests(token)
    }

    @Test
    fun `unmuteGuests returns result on 200`() = runTest {
        val results = listOf(true, false)
        results.forEach { result ->
            server.enqueue {
                setResponseCode(200)
                setBody(json.encodeToString(Box(result)))
            }
            assertThat(step.unmuteGuests(token).await(), "result").isEqualTo(result)
            server.verifyUnmuteGuests(token)
        }
    }

    @Test
    fun `setGuestsCanUnmute throws IllegalStateException`() = runTest {
        server.enqueue { setResponseCode(500) }
        val request = SetGuestCanUnmuteRequest(Random.nextBoolean())
        assertFailure { step.setGuestsCanUnmute(request, token).await() }
            .isInstanceOf<IllegalStateException>()
        server.verifySetGuestsCanUnmute(request, token)
    }

    @Test
    fun `setGuestsCanUnmute throws NoSuchNodeException`() = runTest {
        server.enqueue { setResponseCode(404) }
        val request = SetGuestCanUnmuteRequest(Random.nextBoolean())
        assertFailure { step.setGuestsCanUnmute(request, token).await() }
            .isInstanceOf<NoSuchNodeException>()
        server.verifySetGuestsCanUnmute(request, token)
    }

    @Test
    fun `setGuestsCanUnmute throws NoSuchConferenceException`() = runTest {
        val message = "Neither conference nor gateway found"
        server.enqueue {
            setResponseCode(404)
            setBody(json.encodeToString(Box(message)))
        }
        val request = SetGuestCanUnmuteRequest(Random.nextBoolean())
        assertFailure { step.setGuestsCanUnmute(request, token).await() }
            .isInstanceOf<NoSuchConferenceException>()
            .hasMessage(message)
        server.verifySetGuestsCanUnmute(request, token)
    }

    @Test
    fun `setGuestsCanUnmute throws InvalidTokenException`() = runTest {
        val message = "Invalid token"
        server.enqueue {
            setResponseCode(403)
            setBody(json.encodeToString(Box(message)))
        }
        val request = SetGuestCanUnmuteRequest(Random.nextBoolean())
        assertFailure { step.setGuestsCanUnmute(request, token).await() }
            .isInstanceOf<InvalidTokenException>()
            .hasMessage(message)
        server.verifySetGuestsCanUnmute(request, token)
    }

    @Test
    fun `setGuestsCanUnmute returns result on 200`() = runTest {
        val results = listOf(true, false)
        results.forEach { result ->
            server.enqueue {
                setResponseCode(200)
                setBody(json.encodeToString(Box(result)))
            }
            val request = SetGuestCanUnmuteRequest(Random.nextBoolean())
            assertThat(step.setGuestsCanUnmute(request, token).await(), "response")
                .isEqualTo(result)
            server.verifySetGuestsCanUnmute(request, token)
        }
    }

    @Test
    fun `disconnect throws IllegalStateException`() = runTest {
        server.enqueue { setResponseCode(500) }
        assertFailure { step.disconnect(token).await() }.isInstanceOf<IllegalStateException>()
        server.verifyDisconnect(token)
    }

    @Test
    fun `disconnect throws NoSuchNodeException`() = runTest {
        server.enqueue { setResponseCode(404) }
        assertFailure { step.disconnect(token).await() }.isInstanceOf<NoSuchNodeException>()
        server.verifyDisconnect(token)
    }

    @Test
    fun `disconnect throws NoSuchConferenceException`() = runTest {
        val message = "Neither conference nor gateway found"
        server.enqueue {
            setResponseCode(404)
            setBody(json.encodeToString(Box(message)))
        }
        assertFailure { step.disconnect(token).await() }
            .isInstanceOf<NoSuchConferenceException>()
            .hasMessage(message)
        server.verifyDisconnect(token)
    }

    @Test
    fun `disconnect throws InvalidTokenException`() = runTest {
        val message = "Invalid token"
        server.enqueue {
            setResponseCode(403)
            setBody(json.encodeToString(Box(message)))
        }
        assertFailure { step.disconnect(token).await() }
            .isInstanceOf<InvalidTokenException>()
            .hasMessage(message)
        server.verifyDisconnect(token)
    }

    @Test
    fun `disconnect returns result on 200`() = runTest {
        val results = listOf(true, false)
        results.forEach { result ->
            server.enqueue {
                setResponseCode(200)
                setBody(json.encodeToString(Box(result)))
            }
            assertThat(step.disconnect(token).await(), "result").isEqualTo(result)
            server.verifyDisconnect(token)
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

    private fun MockWebServer.verifyRefreshToken(token: Token) = takeRequest {
        assertRequestUrl(node) {
            addPathSegments("api/client/v2")
            addPathSegment("conferences")
            addPathSegment(conferenceAlias)
            addPathSegment("refresh_token")
        }
        assertToken(token)
        assertPostEmptyBody()
    }

    private fun MockWebServer.verifyReleaseToken(token: Token) = takeRequest {
        assertRequestUrl(node) {
            addPathSegments("api/client/v2")
            addPathSegment("conferences")
            addPathSegment(conferenceAlias)
            addPathSegment("release_token")
        }
        assertToken(token)
        assertPostEmptyBody()
    }

    private fun MockWebServer.verifyMessage(request: MessageRequest, token: Token) =
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

    private fun MockWebServer.verifyAvailableLayouts(token: Token) = takeRequest {
        assertRequestUrl(node) {
            addPathSegments("api/client/v2")
            addPathSegment("conferences")
            addPathSegment(conferenceAlias)
            addPathSegment("available_layouts")
        }
        assertToken(token)
        assertGet()
    }

    private fun MockWebServer.verifyLayoutSvgs(token: Token) = takeRequest {
        assertRequestUrl(node) {
            addPathSegments("api/client/v2")
            addPathSegment("conferences")
            addPathSegment(conferenceAlias)
            addPathSegment("layout_svgs")
        }
        assertToken(token)
        assertGet()
    }

    private fun MockWebServer.verifyTransformLayout(
        request: TransformLayoutRequest,
        token: Token,
    ) = takeRequest {
        assertRequestUrl(node) {
            addPathSegments("api/client/v2")
            addPathSegment("conferences")
            addPathSegment(conferenceAlias)
            addPathSegment("transform_layout")
        }
        assertToken(token)
        assertPost(json, TransformLayoutRequestSerializer, request)
    }

    private fun MockWebServer.verifyTheme(token: Token) = takeRequest {
        assertRequestUrl(node) {
            addPathSegments("api/client/v2")
            addPathSegment("conferences")
            addPathSegment(conferenceAlias)
            addPathSegment("theme")
            addPathSegment("")
        }
        assertToken(token)
        assertGet()
    }

    private fun MockWebServer.verifyClearAllBuzz(token: Token) = takeRequest {
        assertRequestUrl(node) {
            addPathSegments("api/client/v2")
            addPathSegment("conferences")
            addPathSegment(conferenceAlias)
            addPathSegment("clearallbuzz")
        }
        assertToken(token)
        assertPostEmptyBody()
    }

    private fun MockWebServer.verifyLock(token: Token) = takeRequest {
        assertRequestUrl(node) {
            addPathSegments("api/client/v2")
            addPathSegment("conferences")
            addPathSegment(conferenceAlias)
            addPathSegment("lock")
        }
        assertToken(token)
        assertPostEmptyBody()
    }

    private fun MockWebServer.verifyUnlock(token: Token) = takeRequest {
        assertRequestUrl(node) {
            addPathSegments("api/client/v2")
            addPathSegment("conferences")
            addPathSegment(conferenceAlias)
            addPathSegment("unlock")
        }
        assertToken(token)
        assertPostEmptyBody()
    }

    private fun MockWebServer.verifyMuteGuests(token: Token) = takeRequest {
        assertRequestUrl(node) {
            addPathSegments("api/client/v2")
            addPathSegment("conferences")
            addPathSegment(conferenceAlias)
            addPathSegment("muteguests")
        }
        assertToken(token)
        assertPostEmptyBody()
    }

    private fun MockWebServer.verifyUnmuteGuests(token: Token) = takeRequest {
        assertRequestUrl(node) {
            addPathSegments("api/client/v2")
            addPathSegment("conferences")
            addPathSegment(conferenceAlias)
            addPathSegment("unmuteguests")
        }
        assertToken(token)
        assertPostEmptyBody()
    }

    private fun MockWebServer.verifySetGuestsCanUnmute(
        request: SetGuestCanUnmuteRequest,
        token: Token,
    ) = takeRequest {
        assertRequestUrl(node) {
            addPathSegments("api/client/v2")
            addPathSegment("conferences")
            addPathSegment(conferenceAlias)
            addPathSegment("set_guests_can_unmute")
        }
        assertToken(token)
        assertPost(json, request)
    }

    private fun MockWebServer.verifyDisconnect(token: Token) = takeRequest {
        assertRequestUrl(node) {
            addPathSegments("api/client/v2")
            addPathSegment("conferences")
            addPathSegment(conferenceAlias)
            addPathSegment("disconnect")
        }
        assertToken(token)
        assertPostEmptyBody()
    }
}
