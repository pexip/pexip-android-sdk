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
import assertk.assertions.prop
import assertk.tableOf
import com.pexip.sdk.api.coroutines.await
import com.pexip.sdk.api.infinity.internal.RequiredPinResponse
import com.pexip.sdk.api.infinity.internal.RequiredSsoResponse
import com.pexip.sdk.api.infinity.internal.SsoRedirectResponse
import com.pexip.sdk.api.infinity.internal.TransformLayoutRequestSerializer
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockWebServer
import okio.FileSystem
import org.junit.Rule
import java.util.UUID
import kotlin.random.Random
import kotlin.test.BeforeTest
import kotlin.test.Test

internal class ConferenceStepTest {

    @get:Rule
    val server = MockWebServer()

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
        conferenceAlias = Random.nextString(8)
        json = InfinityService.Json
        val service = InfinityService.create(OkHttpClient(), json)
        token = Random.nextFakeToken()
        step = service.newRequest(node.toUrl()).conference(conferenceAlias)
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
        assertFailure { step.refreshToken(token).execute() }.isInstanceOf<IllegalStateException>()
        server.verifyRefreshToken(token)
    }

    @Test
    fun `refreshToken throws NoSuchNodeException`() {
        server.enqueue { setResponseCode(404) }
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
        assertThat(step.refreshToken(token).execute(), "response").isEqualTo(response)
        server.verifyRefreshToken(token)
    }

    @Test
    fun `releaseToken throws IllegalStateException`() {
        server.enqueue { setResponseCode(500) }
        assertFailure { step.releaseToken(token).execute() }.isInstanceOf<IllegalStateException>()
        server.verifyReleaseToken(token)
    }

    @Test
    fun `releaseToken throws NoSuchNodeException`() {
        server.enqueue { setResponseCode(404) }
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
        assertThat(step.releaseToken(token).execute(), "response").isEqualTo(result)
        server.verifyReleaseToken(token)
    }

    @Test
    fun `message throws IllegalStateException`() {
        server.enqueue { setResponseCode(500) }
        val request = Random.nextMessageRequest()
        assertFailure { step.message(request, token).execute() }
            .isInstanceOf<IllegalStateException>()
        server.verifyMessage(request, token)
    }

    @Test
    fun `message throws NoSuchNodeException`() {
        server.enqueue { setResponseCode(404) }
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
            val request = Random.nextMessageRequest()
            assertThat(step.message(request, token).execute(), "response").isEqualTo(result)
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
            layout = LayoutId(Random.nextString(8)),
            guestLayout = LayoutId(Random.nextString(8)),
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
            layout = LayoutId(Random.nextString(8)),
            guestLayout = LayoutId(Random.nextString(8)),
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
            layout = LayoutId(Random.nextString(8)),
            guestLayout = LayoutId(Random.nextString(8)),
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
            layout = LayoutId(Random.nextString(8)),
            guestLayout = LayoutId(Random.nextString(8)),
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
            layout = LayoutId(Random.nextString(8)),
            guestLayout = LayoutId(Random.nextString(8)),
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
                layout = LayoutId(Random.nextString(8)),
                guestLayout = LayoutId(Random.nextString(8)),
                enableOverlayText = Random.nextBoolean(),
            )
            assertThat(step.transformLayout(request, token).await(), "response").isEqualTo(result)
            server.verifyTransformLayout(request, token)
        }
    }

    @Test
    fun `theme throws IllegalStateException`() {
        server.enqueue { setResponseCode(500) }
        assertFailure { step.theme(token).execute() }.isInstanceOf<IllegalStateException>()
        server.verifyTheme(token)
    }

    @Test
    fun `theme throws NoSuchNodeException`() {
        server.enqueue { setResponseCode(404) }
        assertFailure { step.theme(token).execute() }.isInstanceOf<NoSuchNodeException>()
        server.verifyTheme(token)
    }

    @Test
    fun `theme throws NoSuchConferenceException`() {
        val message = "Neither conference nor gateway found"
        server.enqueue {
            setResponseCode(404)
            setBody(json.encodeToString(Box(message)))
        }
        assertFailure { step.theme(token).execute() }.isInstanceOf<NoSuchConferenceException>()
        server.verifyTheme(token)
    }

    @Test
    fun `theme throws InvalidTokenException`() {
        val message = "Invalid token"
        server.enqueue {
            setResponseCode(403)
            setBody(json.encodeToString(Box(message)))
        }
        assertFailure { step.theme(token).execute() }.isInstanceOf<InvalidTokenException>()
        server.verifyTheme(token)
    }

    @Test
    fun `theme returns empty map on 204`() {
        server.enqueue { setResponseCode(204) }
        assertThat(step.theme(token).execute(), "response").isEmpty()
        server.verifyTheme(token)
    }

    @Test
    fun `theme returns a map on 200`() {
        val map = mapOf(
            Random.nextString(8) to SplashScreenResponse(
                background = BackgroundResponse("background.jpg"),
                elements = List(10) {
                    ElementResponse.Text(
                        color = Random.nextLong(Long.MAX_VALUE),
                        text = Random.nextString(8),
                    )
                },
            ),
        )
        server.enqueue {
            setResponseCode(200)
            setBody(json.encodeToString(Box(map)))
        }
        assertThat(step.theme(token).execute(), "response").isEqualTo(map)
        server.verifyTheme(token)
    }

    @Test
    fun `theme with path returns correct URL with token`() {
        val path = Random.nextString(8)
        val actual = step.theme(path, token)
        val expected = node.newBuilder("/api/client/v2/conferences")!!
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
        assertFailure { step.clearAllBuzz(token).await() }.isInstanceOf<NoSuchConferenceException>()
        server.verifyClearAllBuzz(token)
    }

    @Test
    fun `clearAllBuzz throws InvalidTokenException`() = runTest {
        val message = "Invalid token"
        server.enqueue {
            setResponseCode(403)
            setBody(json.encodeToString(Box(message)))
        }
        assertFailure { step.clearAllBuzz(token).await() }.isInstanceOf<InvalidTokenException>()
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
}
