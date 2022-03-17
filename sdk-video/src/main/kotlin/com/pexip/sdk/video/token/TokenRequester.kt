package com.pexip.sdk.video.token

import com.pexip.sdk.video.internal.Dispatcher
import com.pexip.sdk.video.internal.Json
import com.pexip.sdk.video.internal.OkHttpClient
import com.pexip.sdk.video.internal.StringSerializer
import com.pexip.sdk.video.internal.decodeFromResponseBody
import com.pexip.sdk.video.internal.encodeToRequestBody
import com.pexip.sdk.video.internal.execute
import com.pexip.sdk.video.token.internal.ErrorResponse
import com.pexip.sdk.video.token.internal.RequestToken200ResponseSerializer
import com.pexip.sdk.video.token.internal.RequestToken403ResponseSerializer
import com.pexip.sdk.video.token.internal.RequestTokenRequest
import com.pexip.sdk.video.token.internal.RequiredPinResponse
import com.pexip.sdk.video.token.internal.RequiredSsoResponse
import com.pexip.sdk.video.token.internal.SsoRedirectResponse
import kotlinx.serialization.SerializationException
import okhttp3.OkHttpClient
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.Future

public class TokenRequester private constructor(private val client: OkHttpClient) {

    /**
     * A callback that will be invoked after call to [request].
     */
    public interface Callback {

        /**
         * Invoked when token request completed without issues.
         *
         * @param requester a [TokenRequester] used to perform this operation
         * @param token an instance of [Token]
         */
        public fun onSuccess(requester: TokenRequester, token: Token)

        /**
         * Invoked when node resolution encountered an error.
         *
         * Errors may be one of the following:
         *  - [SsoRedirectException] if SSO flow should continue via the provided URL
         *  - [RequiredSsoException] if SSO authentication is required to proceed
         *  - [RequiredPinException] if either host or guest PIN is required (if PIN was null)
         *  - [InvalidPinException] if the supplied PIN is invalid
         *  - [NoSuchNodeException] if supplied node address doesn't have a deployment
         *  - [NoSuchConferenceException] if alias did not match any aliases or call routing rules
         *  - [IOException] if a network error was encountered during operation
         *
         * @param requester a [TokenRequester] used to perform this operation
         * @param t an error
         */
        public fun onFailure(requester: TokenRequester, t: Throwable)
    }

    /**
     * Requests a new token from the conferencing node.
     *
     * @param request a [TokenRequest] spec
     * @param callback a completion handler
     * @return a [Future] that may be used to cancel the operation
     */
    public fun request(request: TokenRequest, callback: Callback): Future<*> {
        val runnable = RequestRunnable(this, request, callback)
        return Dispatcher.submit(runnable)
    }

    private class RequestRunnable(
        private val requester: TokenRequester,
        private val request: TokenRequest,
        private val callback: Callback,
    ) : Runnable {

        override fun run() = try {
            val response = requester.client.execute {
                with(request) {
                    val r = RequestTokenRequest(
                        display_name = joinDetails.displayName,
                        conference_extension = joinDetails.alias,
                        chosen_idp = idp?.uuid,
                        sso_token = ssoToken
                    )
                    post(Json.encodeToRequestBody(r))
                    url(url)
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
                node = request.node,
                joinDetails = request.joinDetails,
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

    public class Builder {

        private var client: OkHttpClient? = null

        /**
         * Sets the [OkHttpClient] used to make the calls.
         *
         * @param client an instance of [OkHttpClient]
         * @return this [Builder]
         */
        public fun client(client: OkHttpClient): Builder = apply {
            this.client = client
        }

        public fun build(): TokenRequester = TokenRequester(client ?: OkHttpClient)
    }
}
