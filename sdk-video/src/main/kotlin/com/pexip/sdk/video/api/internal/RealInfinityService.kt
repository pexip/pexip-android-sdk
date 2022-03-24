package com.pexip.sdk.video.api.internal

import com.pexip.sdk.video.api.Call
import com.pexip.sdk.video.api.CallId
import com.pexip.sdk.video.api.CallsRequest
import com.pexip.sdk.video.api.CallsResponse
import com.pexip.sdk.video.api.ConferenceAlias
import com.pexip.sdk.video.api.InfinityService
import com.pexip.sdk.video.api.InfinityService.CallStep
import com.pexip.sdk.video.api.InfinityService.ConferenceStep
import com.pexip.sdk.video.api.InfinityService.ParticipantStep
import com.pexip.sdk.video.api.InfinityService.RequestBuilder
import com.pexip.sdk.video.api.InvalidPinException
import com.pexip.sdk.video.api.InvalidTokenException
import com.pexip.sdk.video.api.NewCandidateRequest
import com.pexip.sdk.video.api.NoSuchConferenceException
import com.pexip.sdk.video.api.NoSuchNodeException
import com.pexip.sdk.video.api.Node
import com.pexip.sdk.video.api.ParticipantId
import com.pexip.sdk.video.api.RefreshTokenResponse
import com.pexip.sdk.video.api.RequestTokenRequest
import com.pexip.sdk.video.api.RequestTokenResponse
import com.pexip.sdk.video.api.RequiredPinException
import com.pexip.sdk.video.api.RequiredSsoException
import com.pexip.sdk.video.api.SsoRedirectException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.internal.EMPTY_REQUEST

internal class RealInfinityService(
    private val client: OkHttpClient,
    private val json: Json = Json { ignoreUnknownKeys = true },
) : InfinityService {

    override fun newRequest(node: Node): RequestBuilder =
        RealRequestBuilder(client, json, node.address)

    private class RealRequestBuilder(
        private val client: OkHttpClient,
        private val json: Json,
        url: HttpUrl,
    ) : RequestBuilder, ConferenceStep, ParticipantStep, CallStep {

        private var builder = url.newBuilder()
            .addPathSegment("api")
            .addPathSegment("client")
            .addPathSegment("v2")

        override fun status(): Call<Boolean> = RealCall(
            call = client.newCall {
                get()
                url(builder.addPathSegment("status").build())
            },
            mapper = ::parseStatus
        )

        override fun conference(conferenceAlias: ConferenceAlias): ConferenceStep =
            apply {
                builder
                    .addPathSegment("conferences")
                    .addPathSegment(conferenceAlias.value)
            }

        override fun requestToken(request: RequestTokenRequest): Call<RequestTokenResponse> =
            RealCall(
                call = client.newCall {
                    post(json.encodeToRequestBody(request))
                    url(builder.addPathSegment("request_token").build())
                },
                mapper = ::parseRequestToken
            )

        override fun requestToken(
            request: RequestTokenRequest,
            pin: String,
        ): Call<RequestTokenResponse> {
            require(pin.isNotBlank()) { "pin is blank." }
            return RealCall(
                call = client.newCall {
                    post(json.encodeToRequestBody(request))
                    url(builder.addPathSegment("request_token").build())
                    header("pin", pin.trim())
                },
                mapper = ::parseRequestToken
            )
        }

        override fun refreshToken(token: String): Call<RefreshTokenResponse> {
            require(token.isNotBlank()) { "token is blank." }
            return RealCall(
                call = client.newCall {
                    post(EMPTY_REQUEST)
                    url(builder.addPathSegment("refresh_token").build())
                    header("token", token)
                },
                mapper = ::parseRefreshToken
            )
        }

        override fun releaseToken(token: String): Call<Unit> {
            require(token.isNotBlank()) { "token is blank." }
            return RealCall(
                call = client.newCall {
                    post(EMPTY_REQUEST)
                    url(builder.addPathSegment("release_token").build())
                    header("token", token)
                },
                mapper = unitMapper
            )
        }

        override fun participant(participantId: ParticipantId): ParticipantStep =
            apply {
                builder
                    .addPathSegment("participants")
                    .addPathSegment(participantId.value)
            }

        override fun calls(
            request: CallsRequest,
            token: String,
        ): Call<CallsResponse> {
            require(token.isNotBlank()) { "token is blank." }
            return RealCall(
                call = client.newCall {
                    post(json.encodeToRequestBody(request))
                    url(builder.addPathSegment("calls").build())
                    header("token", token)
                },
                mapper = ::parseCalls
            )
        }

        override fun call(callId: CallId): CallStep = apply {
            builder
                .addPathSegment("calls")
                .addPathSegment(callId.value)
        }

        override fun ack(token: String): Call<Unit> {
            require(token.isNotBlank()) { "token is blank." }
            return RealCall(
                call = client.newCall {
                    post(EMPTY_REQUEST)
                    url(builder.addPathSegment("ack").build())
                    header("token", token)
                },
                mapper = unitMapper
            )
        }

        override fun newCandidate(
            request: NewCandidateRequest,
            token: String,
        ): Call<Unit> {
            require(token.isNotBlank()) { "token is blank." }
            return RealCall(
                call = client.newCall {
                    post(json.encodeToRequestBody(request))
                    url(builder.addPathSegment("new_candidate").build())
                    header("token", token)
                },
                mapper = unitMapper
            )
        }

        private fun parseStatus(response: Response) = when (response.code) {
            200 -> false
            404 -> throw NoSuchNodeException()
            503 -> true
            else -> throw IllegalStateException()
        }

        private fun parseRequestToken(response: Response) = when (response.code) {
            200 -> response.parseRequestToken200()
            403 -> response.parseRequestToken403()
            404 -> response.parseRequestToken404()
            else -> throw IllegalStateException()
        }

        private fun Response.parseRequestToken200() =
            json.decodeFromResponseBody(RequestTokenResponseSerializer, body!!)

        private fun Response.parseRequestToken403(): Nothing {
            val r = json.decodeFromResponseBody(RequestToken403ResponseSerializer, body!!)
            throw when (r) {
                is RequiredPinResponse -> RequiredPinException(r.guest_pin == "required")
                is RequiredSsoResponse -> RequiredSsoException(r.idp)
                is SsoRedirectResponse -> SsoRedirectException(r.redirect_url, r.redirect_idp)
                is ErrorResponse -> InvalidPinException(r.message)
            }
        }

        private fun Response.parseRequestToken404(): Nothing = try {
            val message = json.decodeFromResponseBody(StringSerializer, body!!)
            throw NoSuchConferenceException(message)
        } catch (e: SerializationException) {
            throw NoSuchNodeException()
        }

        private fun parseRefreshToken(response: Response) = when (response.code) {
            200 -> response.parseRefreshToken200()
            403 -> response.parseRefreshToken403()
            404 -> response.parseRefreshToken404()
            else -> throw IllegalStateException()
        }

        private fun Response.parseRefreshToken200() =
            json.decodeFromResponseBody(RefreshTokenResponseSerializer, body!!)

        private fun Response.parseRefreshToken403(): Nothing {
            val message = json.decodeFromResponseBody(StringSerializer, body!!)
            throw InvalidTokenException(message)
        }

        private fun Response.parseRefreshToken404(): Nothing = try {
            val message = json.decodeFromResponseBody(StringSerializer, body!!)
            throw NoSuchConferenceException(message)
        } catch (e: SerializationException) {
            throw NoSuchNodeException()
        }

        private fun parseCalls(response: Response) = when (response.code) {
            200 -> response.parseCalls200()
            else -> throw IllegalStateException()
        }

        private fun Response.parseCalls200() =
            json.decodeFromResponseBody(CallsResponseSerializer, body!!)

        private val unitMapper: (Response) -> Unit = {}
    }
}
