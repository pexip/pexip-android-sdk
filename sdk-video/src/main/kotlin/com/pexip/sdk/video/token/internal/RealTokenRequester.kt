package com.pexip.sdk.video.token.internal

import com.pexip.sdk.video.internal.Dispatcher
import com.pexip.sdk.video.internal.Json
import com.pexip.sdk.video.internal.StringSerializer
import com.pexip.sdk.video.internal.decodeFromResponseBody
import com.pexip.sdk.video.internal.encodeToRequestBody
import com.pexip.sdk.video.internal.execute
import com.pexip.sdk.video.internal.url
import com.pexip.sdk.video.token.InvalidPinException
import com.pexip.sdk.video.token.NoSuchConferenceException
import com.pexip.sdk.video.token.NoSuchNodeException
import com.pexip.sdk.video.token.RequiredPinException
import com.pexip.sdk.video.token.RequiredSsoException
import com.pexip.sdk.video.token.SsoRedirectException
import com.pexip.sdk.video.token.Token
import com.pexip.sdk.video.token.TokenRequest
import com.pexip.sdk.video.token.TokenRequester
import kotlinx.serialization.SerializationException
import okhttp3.OkHttpClient
import okhttp3.Response
import java.util.concurrent.Future

internal class RealTokenRequester(private val client: OkHttpClient) : TokenRequester {

    override fun request(request: TokenRequest, callback: TokenRequester.Callback): Future<*> {
        val runnable = RequestRunnable(this, request, callback)
        return Dispatcher.submit(runnable)
    }

    private class RequestRunnable(
        private val requester: RealTokenRequester,
        private val request: TokenRequest,
        private val callback: TokenRequester.Callback,
    ) : Runnable {

        override fun run() = try {
            val response = requester.client.execute {
                with(request) {
                    val r = RequestTokenRequest(
                        display_name = displayName,
                        conference_extension = alias,
                        chosen_idp = idp?.uuid,
                        sso_token = ssoToken
                    )
                    post(Json.encodeToRequestBody(r))
                    url(conferenceAddress) { addPathSegment("request_token") }
                    pin?.let { header("pin", it.trim()) }
                }
            }
            val token = response.use { it.parse(request) }
            callback.onSuccess(requester, token)
        } catch (t: Throwable) {
            callback.onFailure(requester, t)
        }

        private fun Response.parse(request: TokenRequest) = when (code) {
            200 -> parse200(request)
            403 -> parse403()
            404 -> parse404()
            else -> throw IllegalStateException()
        }

        private fun Response.parse200(request: TokenRequest): Token {
            val response = Json.decodeFromResponseBody(RequestToken200ResponseSerializer, body!!)
            return Token(
                address = request.conferenceAddress,
                participantId = response.participant_uuid,
                token = response.token,
                expires = response.expires
            )
        }

        private fun Response.parse403(): Nothing {
            val r = Json.decodeFromResponseBody(RequestToken403ResponseSerializer, body!!)
            throw when (r) {
                is RequiredPinResponse -> RequiredPinException(r.guest_pin == "required")
                is RequiredSsoResponse -> RequiredSsoException(r.idp)
                is SsoRedirectResponse -> SsoRedirectException(r.redirect_url, r.redirect_idp)
                is ErrorResponse -> InvalidPinException(r.message)
            }
        }

        private fun Response.parse404(): Nothing = try {
            val message = Json.decodeFromResponseBody(StringSerializer, body!!)
            throw NoSuchConferenceException(message)
        } catch (e: SerializationException) {
            throw NoSuchNodeException()
        }
    }
}
