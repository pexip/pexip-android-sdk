package com.pexip.sdk.video

import com.pexip.sdk.video.internal.Json
import com.pexip.sdk.video.internal.OkHttpClient
import com.pexip.sdk.video.internal.RequestToken200Serializer
import com.pexip.sdk.video.internal.RequestToken403Serializer
import com.pexip.sdk.video.internal.RequestTokenRequest
import com.pexip.sdk.video.internal.RequiredPinResponse
import com.pexip.sdk.video.internal.RequiredSsoResponse
import com.pexip.sdk.video.internal.SsoRedirectResponse
import com.pexip.sdk.video.internal.StringSerializer
import com.pexip.sdk.video.internal.await
import com.pexip.sdk.video.internal.decodeFromResponseBody
import com.pexip.sdk.video.internal.encodeToRequestBody
import kotlinx.serialization.SerializationException
import okhttp3.OkHttpClient
import okhttp3.Response
import java.io.IOException

public class TokenRequester private constructor(private val client: OkHttpClient) {

    /**
     * Requests a new token from the conferencing node.
     *
     * @param request a [TokenRequest] spec
     * @return a [Token] for this conference
     * @throws SsoRedirectException if SSO flow should continue via the provided URL
     * @throws RequiredSsoException if SSO authentication is required to proceed
     * @throws RequiredPinException if either host or guest PIN is required (if PIN was null)
     * @throws InvalidPinException if the supplied PIN is invalid
     * @throws NoSuchNodeException if supplied node address doesn't have a deployment
     * @throws NoSuchConferenceException if alias did not match any aliases or call routing rules
     * @throws IOException if a network error was encountered during operation
     */
    public suspend fun requestToken(request: TokenRequest): Token {
        val response = client.await {
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
        return response.use { it.parse(request) }
    }

    private fun Response.parse(request: TokenRequest) = when (code) {
        200 -> parse200(request)
        403 -> parse403()
        404 -> parse404()
        else -> throw IllegalStateException()
    }

    private fun Response.parse200(request: TokenRequest): Token {
        val response = Json.decodeFromResponseBody(RequestToken200Serializer, body!!)
        return Token(
            node = request.node,
            joinDetails = request.joinDetails,
            value = response.token,
        )
    }

    private fun Response.parse403(): Nothing {
        throw when (val r = Json.decodeFromResponseBody(RequestToken403Serializer, body!!)) {
            is RequiredPinResponse -> RequiredPinException(r.guest_pin == "required")
            is RequiredSsoResponse -> RequiredSsoException(r.idp)
            is SsoRedirectResponse -> SsoRedirectException(r.redirect_url, r.redirect_idp)
            is String -> InvalidPinException(r)
            else -> SerializationException("Failed to deserialize body.")
        }
    }

    private fun Response.parse404(): Nothing = try {
        val message = Json.decodeFromResponseBody(StringSerializer, body!!)
        throw NoSuchConferenceException(message)
    } catch (e: SerializationException) {
        throw NoSuchNodeException()
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
