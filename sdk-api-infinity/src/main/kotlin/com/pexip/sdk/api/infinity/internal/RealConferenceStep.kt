package com.pexip.sdk.api.infinity.internal

import com.pexip.sdk.api.Call
import com.pexip.sdk.api.infinity.InfinityService
import com.pexip.sdk.api.infinity.InvalidPinException
import com.pexip.sdk.api.infinity.InvalidTokenException
import com.pexip.sdk.api.infinity.NoSuchConferenceException
import com.pexip.sdk.api.infinity.NoSuchNodeException
import com.pexip.sdk.api.infinity.RefreshTokenResponse
import com.pexip.sdk.api.infinity.RequestTokenRequest
import com.pexip.sdk.api.infinity.RequestTokenResponse
import com.pexip.sdk.api.infinity.RequiredPinException
import com.pexip.sdk.api.infinity.RequiredSsoException
import com.pexip.sdk.api.infinity.SsoRedirectException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.internal.EMPTY_REQUEST
import java.net.URL
import java.util.UUID

internal class RealConferenceStep(
    private val client: OkHttpClient,
    private val json: Json,
    private val node: URL,
    private val conferenceAlias: String,
) : InfinityService.ConferenceStep {

    override fun requestToken(request: RequestTokenRequest): Call<RequestTokenResponse> = RealCall(
        call = client.newCall {
            post(json.encodeToRequestBody(request))
            url(node, conferenceAlias, "request_token")
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
                url(node, conferenceAlias, "request_token")
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
                url(node, conferenceAlias, "refresh_token")
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
                url(node, conferenceAlias, "release_token")
                header("token", token)
            },
            mapper = ::parseReleaseToken
        )
    }

    override fun participant(participantId: UUID): InfinityService.ParticipantStep =
        RealParticipantStep(client, json, node, conferenceAlias, participantId)

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
        200 -> Unit
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
