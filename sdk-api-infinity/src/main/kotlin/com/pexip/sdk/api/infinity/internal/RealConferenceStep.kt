package com.pexip.sdk.api.infinity.internal

import com.pexip.sdk.api.Call
import com.pexip.sdk.api.EventSourceFactory
import com.pexip.sdk.api.infinity.InfinityService
import com.pexip.sdk.api.infinity.InvalidPinException
import com.pexip.sdk.api.infinity.InvalidTokenException
import com.pexip.sdk.api.infinity.MessageRequest
import com.pexip.sdk.api.infinity.NoSuchConferenceException
import com.pexip.sdk.api.infinity.NoSuchNodeException
import com.pexip.sdk.api.infinity.RefreshTokenResponse
import com.pexip.sdk.api.infinity.RequestTokenRequest
import com.pexip.sdk.api.infinity.RequestTokenResponse
import com.pexip.sdk.api.infinity.RequiredPinException
import com.pexip.sdk.api.infinity.RequiredSsoException
import com.pexip.sdk.api.infinity.SsoRedirectException
import com.pexip.sdk.api.infinity.Token
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.internal.EMPTY_REQUEST
import java.util.UUID

internal class RealConferenceStep(
    private val client: OkHttpClient,
    private val json: Json,
    private val url: HttpUrl,
) : InfinityService.ConferenceStep {

    override fun requestToken(request: RequestTokenRequest): Call<RequestTokenResponse> {
        val builder = Request.Builder()
            .post(json.encodeToRequestBody(request))
            .url(HttpUrl(url) { addPathSegment("request_token") })
        if (request.incomingToken?.isNotBlank() == true) {
            builder.header("token", request.incomingToken)
        }
        return RealCall(client, builder.build(), ::parseRequestToken)
    }

    override fun requestToken(
        request: RequestTokenRequest,
        pin: String,
    ): Call<RequestTokenResponse> = RealCall(
        client = client,
        request = Request.Builder()
            .post(json.encodeToRequestBody(request))
            .url(HttpUrl(url) { addPathSegment("request_token") })
            .header("pin", if (pin.isBlank()) "none" else pin.trim())
            .build(),
        mapper = ::parseRequestToken
    )

    override fun refreshToken(token: String): Call<RefreshTokenResponse> {
        require(token.isNotBlank()) { "token is blank." }
        return RealCall(
            client = client,
            request = Request.Builder()
                .post(EMPTY_REQUEST)
                .url(HttpUrl(url) { addPathSegment("refresh_token") })
                .header("token", token)
                .build(),
            mapper = ::parseRefreshToken
        )
    }

    override fun refreshToken(token: Token): Call<RefreshTokenResponse> = refreshToken(token.token)

    override fun releaseToken(token: String): Call<Boolean> {
        require(token.isNotBlank()) { "token is blank." }
        return RealCall(
            client = client,
            request = Request.Builder()
                .post(EMPTY_REQUEST)
                .url(HttpUrl(url) { addPathSegment("release_token") })
                .header("token", token)
                .build(),
            mapper = ::parseReleaseToken
        )
    }

    override fun releaseToken(token: Token): Call<Boolean> = releaseToken(token.token)

    override fun message(request: MessageRequest, token: String): Call<Boolean> {
        require(token.isNotBlank()) { "token is blank." }
        return RealCall(
            client = client,
            request = Request.Builder()
                .post(json.encodeToRequestBody(request))
                .url(HttpUrl(url) { addPathSegment("message") })
                .header("token", token)
                .build(),
            mapper = ::parseMessage
        )
    }

    override fun message(request: MessageRequest, token: Token): Call<Boolean> =
        message(request, token.token)

    override fun events(token: String): EventSourceFactory {
        require(token.isNotBlank()) { "token is blank." }
        return RealEventSourceFactory(
            client = client,
            request = Request.Builder()
                .get()
                .url(HttpUrl(url) { addPathSegment("events") })
                .header("token", token)
                .build(),
            json = json
        )
    }

    override fun events(token: Token): EventSourceFactory = events(token.token)

    override fun participant(participantId: UUID): InfinityService.ParticipantStep =
        RealParticipantStep(
            client = client,
            json = json,
            url = HttpUrl(url) {
                addPathSegment("participants")
                addPathSegment(participantId.toString())
            }
        )

    private fun parseRequestToken(response: Response) = when (response.code) {
        200 -> json.decodeFromResponseBody(RequestTokenResponseSerializer, response.body!!)
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

    private fun parseRefreshToken(response: Response) = when (response.code) {
        200 -> json.decodeFromResponseBody(RefreshTokenResponseSerializer, response.body!!)
        403 -> response.parse403()
        404 -> response.parse404()
        else -> throw IllegalStateException()
    }

    private fun parseReleaseToken(response: Response) = when (response.code) {
        200 -> json.decodeFromResponseBody(BooleanSerializer, response.body!!)
        403 -> response.parse403()
        404 -> response.parse404()
        else -> throw IllegalStateException()
    }

    private fun parseMessage(response: Response) = when (response.code) {
        200 -> json.decodeFromResponseBody(BooleanSerializer, response.body!!)
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
