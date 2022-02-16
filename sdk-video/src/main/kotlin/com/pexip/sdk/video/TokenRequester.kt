package com.pexip.sdk.video

import com.pexip.sdk.video.internal.Box
import com.pexip.sdk.video.internal.Json
import com.pexip.sdk.video.internal.OkHttpClient
import com.pexip.sdk.video.internal.RequestToken403Serializer
import com.pexip.sdk.video.internal.RequestTokenRequest
import com.pexip.sdk.video.internal.RequiredPinResponse
import com.pexip.sdk.video.internal.RequiredSsoResponse
import com.pexip.sdk.video.internal.SsoRedirectResponse
import com.pexip.sdk.video.internal.await
import com.pexip.sdk.video.internal.decodeFromResponseBody
import com.pexip.sdk.video.internal.encodeToRequestBody
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JsonElement
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
                    display_name = displayName,
                    conference_extension = alias,
                    chosen_idp = idp?.uuid,
                    sso_token = ssoToken
                )
                post(Json.encodeToRequestBody(r))
                url(url)
                pin?.let { header("pin", it.trim()) }
            }
        }
        return response.use { it.parse() }
    }

    private fun Response.parse() = when (code) {
        200 -> parse200()
        403 -> parse403()
        404 -> parse404()
        else -> throw IllegalStateException()
    }

    private fun Response.parse200() = Json.decodeFromResponseBody<Box<Token>>(body!!).result

    private fun Response.parse403(): Nothing {
        val (element) = Json.decodeFromResponseBody<Box<JsonElement>>(body!!)
        throw when (val r = Json.decodeFromJsonElement(RequestToken403Serializer, element)) {
            is RequiredPinResponse -> RequiredPinException(r.guest_pin == "required")
            is RequiredSsoResponse -> RequiredSsoException(r.idp)
            is SsoRedirectResponse -> SsoRedirectException(r.redirect_url, r.redirect_idp)
            is String -> InvalidPinException(r)
            else -> SerializationException("Failed to deserialize body.")
        }
    }

    private fun Response.parse404(): Nothing = try {
        val (message) = Json.decodeFromResponseBody<Box<String>>(body!!)
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
