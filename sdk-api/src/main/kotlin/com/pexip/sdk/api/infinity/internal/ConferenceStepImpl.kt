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
package com.pexip.sdk.api.infinity.internal

import com.pexip.sdk.api.Call
import com.pexip.sdk.api.EventSourceFactory
import com.pexip.sdk.api.infinity.IllegalLayoutTransformException
import com.pexip.sdk.api.infinity.InfinityService
import com.pexip.sdk.api.infinity.InvalidPinException
import com.pexip.sdk.api.infinity.InvalidTokenException
import com.pexip.sdk.api.infinity.LayoutId
import com.pexip.sdk.api.infinity.MessageRequest
import com.pexip.sdk.api.infinity.NoSuchConferenceException
import com.pexip.sdk.api.infinity.NoSuchNodeException
import com.pexip.sdk.api.infinity.RefreshTokenResponse
import com.pexip.sdk.api.infinity.RequestTokenRequest
import com.pexip.sdk.api.infinity.RequestTokenResponse
import com.pexip.sdk.api.infinity.RequiredPinException
import com.pexip.sdk.api.infinity.RequiredSsoException
import com.pexip.sdk.api.infinity.SplashScreenResponse
import com.pexip.sdk.api.infinity.SsoRedirectException
import com.pexip.sdk.api.infinity.Token
import com.pexip.sdk.api.infinity.TransformLayoutRequest
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationException
import okhttp3.Request
import okhttp3.Response
import okhttp3.internal.EMPTY_REQUEST
import java.util.UUID

internal class ConferenceStepImpl(
    override val requestBuilder: RequestBuilderImpl,
    override val conferenceAlias: String,
) : InfinityService.ConferenceStep,
    ConferenceStepImplScope,
    RequestBuilderImplScope by requestBuilder {

    override fun requestToken(request: RequestTokenRequest): Call<RequestTokenResponse> = RealCall(
        client = client,
        request = Request.Builder()
            .post(json.encodeToRequestBody(request))
            .url(node) {
                conference(conferenceAlias)
                addPathSegment("request_token")
            }
            .withTag(request.directMedia)
            .build(),
        mapper = ::parseRequestToken,
    )

    override fun requestToken(
        request: RequestTokenRequest,
        pin: String,
    ): Call<RequestTokenResponse> = RealCall(
        client = client,
        request = Request.Builder()
            .post(json.encodeToRequestBody(request))
            .url(node) {
                conference(conferenceAlias)
                addPathSegment("request_token")
            }
            .withTag(request.directMedia)
            .header("pin", if (pin.isBlank()) "none" else pin.trim())
            .build(),
        mapper = ::parseRequestToken,
    )

    override fun refreshToken(token: Token): Call<RefreshTokenResponse> = RealCall(
        client = client,
        request = Request.Builder()
            .post(EMPTY_REQUEST)
            .url(node) {
                conference(conferenceAlias)
                addPathSegment("refresh_token")
            }
            .token(token)
            .build(),
        mapper = { parse(it, RefreshTokenResponseSerializer) },
    )

    override fun releaseToken(token: Token): Call<Boolean> = RealCall(
        client = client,
        request = Request.Builder()
            .post(EMPTY_REQUEST)
            .url(node) {
                conference(conferenceAlias)
                addPathSegment("release_token")
            }
            .token(token)
            .build(),
        mapper = { parse(it, BooleanSerializer) },
    )

    override fun message(request: MessageRequest, token: Token): Call<Boolean> = RealCall(
        client = client,
        request = Request.Builder()
            .post(json.encodeToRequestBody(request))
            .url(node) {
                conference(conferenceAlias)
                addPathSegment("message")
            }
            .token(token)
            .build(),
        mapper = { parse(it, BooleanSerializer) },
    )

    override fun availableLayouts(token: Token): Call<Set<LayoutId>> = RealCall(
        client = client,
        request = Request.Builder()
            .get()
            .url(node) {
                conference(conferenceAlias)
                addPathSegment("available_layouts")
            }
            .token(token)
            .build(),
        mapper = { parse(it, AvailableLayoutsSerializer) },
    )

    override fun layoutSvgs(token: Token): Call<Map<LayoutId, String>> = RealCall(
        client = client,
        request = Request.Builder()
            .get()
            .url(node) {
                conference(conferenceAlias)
                addPathSegment("layout_svgs")
            }
            .token(token)
            .build(),
        mapper = { parse(it, LayoutSvgsSerializer) },
    )

    override fun transformLayout(request: TransformLayoutRequest, token: Token): Call<Boolean> =
        RealCall(
            client = client,
            request = Request.Builder()
                .post(json.encodeToRequestBody(TransformLayoutRequestSerializer, request))
                .url(node) {
                    conference(conferenceAlias)
                    addPathSegment("transform_layout")
                }
                .token(token)
                .build(),
            mapper = ::parseTransformLayout,
        )

    override fun theme(token: Token): Call<Map<String, SplashScreenResponse>> = RealCall(
        client = client,
        request = Request.Builder()
            .get()
            .url(node) {
                conference(conferenceAlias)
                addPathSegment("theme")
                addPathSegment("")
            }
            .token(token)
            .build(),
        mapper = ::parseTheme,
    )

    override fun theme(path: String, token: Token): String {
        require(path.isNotBlank()) { "path is blank." }
        return node.newApiClientV2Builder()
            .conference(conferenceAlias)
            .addPathSegment("theme")
            .addPathSegment(path)
            .token(token)
            .toString()
    }

    override fun clearAllBuzz(token: Token): Call<Boolean> = RealCall(
        client = client,
        request = Request.Builder()
            .post(EMPTY_REQUEST)
            .url(node) {
                conference(conferenceAlias)
                addPathSegment("clearallbuzz")
            }
            .token(token)
            .build(),
        mapper = { parse(it, BooleanSerializer) },
    )

    override fun lock(token: Token): Call<Boolean> = RealCall(
        client = client,
        request = Request.Builder()
            .post(EMPTY_REQUEST)
            .url(node) {
                conference(conferenceAlias)
                addPathSegment("lock")
            }
            .token(token)
            .build(),
        mapper = { parse(it, BooleanSerializer) },
    )

    override fun unlock(token: Token): Call<Boolean> = RealCall(
        client = client,
        request = Request.Builder()
            .post(EMPTY_REQUEST)
            .url(node) {
                conference(conferenceAlias)
                addPathSegment("unlock")
            }
            .token(token)
            .build(),
        mapper = { parse(it, BooleanSerializer) },
    )

    override fun muteGuests(token: Token): Call<Boolean> = RealCall(
        client = client,
        request = Request.Builder()
            .post(EMPTY_REQUEST)
            .url(node) {
                conference(conferenceAlias)
                addPathSegment("muteguests")
            }
            .token(token)
            .build(),
        mapper = { parse(it, BooleanSerializer) },
    )

    override fun unmuteGuests(token: Token): Call<Boolean> = RealCall(
        client = client,
        request = Request.Builder()
            .post(EMPTY_REQUEST)
            .url(node) {
                conference(conferenceAlias)
                addPathSegment("unmuteguests")
            }
            .token(token)
            .build(),
        mapper = { parse(it, BooleanSerializer) },
    )

    override fun disconnect(token: Token): Call<Boolean> = RealCall(
        client = client,
        request = Request.Builder()
            .post(EMPTY_REQUEST)
            .url(node) {
                conference(conferenceAlias)
                addPathSegment("disconnect")
            }
            .token(token)
            .build(),
        mapper = { parse(it, BooleanSerializer) },
    )

    override fun events(token: Token): EventSourceFactory = RealEventSourceFactory(
        client = client,
        request = Request.Builder()
            .get()
            .url(node) {
                conference(conferenceAlias)
                addPathSegment("events")
            }
            .token(token)
            .build(),
        json = json,
    )

    override fun participant(participantId: UUID): InfinityService.ParticipantStep =
        ParticipantStepImpl(this, participantId)

    private fun parseRequestToken(response: Response) = when (response.code) {
        200 -> {
            val r = json.decodeFromResponseBody(RequestTokenResponseSerializer, response.body!!)
            val directMediaRequested = response.request.tagOrElse { false }
            r.copy(directMediaRequested = directMediaRequested)
        }
        403 -> response.parseRequestToken403()
        404 -> response.parse404()
        else -> throw IllegalStateException()
    }

    private fun Response.parseRequestToken403(): Nothing {
        val r = json.decodeFromResponseBody(RequestToken403ResponseSerializer, body!!)
        throw when (r) {
            is RequiredPinResponse -> RequiredPinException(r.guest_pin == "required")
            is RequiredSsoResponse -> RequiredSsoException(r.idp)
            is SsoRedirectResponse -> SsoRedirectException(r.redirect_url, r.redirect_idp)
            is ErrorResponse -> InvalidPinException(r.message)
        }
    }

    private fun parseTransformLayout(response: Response) = when (response.code) {
        400 -> {
            val message = json.decodeFromResponseBody(StringSerializer, response.body!!)
            throw IllegalLayoutTransformException(message)
        }
        else -> parse(response, BooleanSerializer)
    }

    private fun parseTheme(response: Response) = when (response.code) {
        204 -> mapOf()
        else -> parse(response, ThemeSerializer)
    }

    private inline fun <reified T> parse(
        response: Response,
        deserializer: DeserializationStrategy<T>,
    ) = when (response.code) {
        200 -> json.decodeFromResponseBody(deserializer, response.body!!)
        403 -> response.parse403()
        404 -> response.parse404()
        else -> throw IllegalStateException()
    }

    private fun Response.parse403(): Nothing {
        val message = json.decodeFromResponseBody(StringSerializer, body!!)
        throw InvalidTokenException(message)
    }

    private fun Response.parse404(): Nothing = try {
        val message = json.decodeFromResponseBody(StringSerializer, body!!)
        throw NoSuchConferenceException(message)
    } catch (e: SerializationException) {
        throw NoSuchNodeException()
    }
}
