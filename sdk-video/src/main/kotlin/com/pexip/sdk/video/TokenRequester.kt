package com.pexip.sdk.video

import com.pexip.sdk.video.internal.Box
import com.pexip.sdk.video.internal.Json
import com.pexip.sdk.video.internal.OkHttpClient
import com.pexip.sdk.video.internal.RequestToken403Response
import com.pexip.sdk.video.internal.RequestTokenRequest
import com.pexip.sdk.video.internal.await
import com.pexip.sdk.video.internal.decodeFromResponseBody
import com.pexip.sdk.video.internal.encodeToRequestBody
import kotlinx.serialization.SerializationException
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import java.io.IOException

public class TokenRequester private constructor(private val client: OkHttpClient) {

    /**
     * Requests a new token from the conferencing node.
     *
     * @param nodeAddress a node address in the form of https://example.com
     * @param alias a conference alias
     * @param displayName a name for this participant
     * @param pin an optional PIN
     * @return a [Token] for this conference
     * @throws RequiredPinException if either host or guest PIN is required (if [pin] was null)
     * @throws InvalidPinException if the supplied [pin] is invalid
     * @throws NoSuchNodeException if supplied [nodeAddress] doesn't have a deployment
     * @throws NoSuchConferenceException if supplied [alias] did not match any aliases or
     * call routing rules
     * @throws IOException if a network error was encountered during operation
     */
    public suspend fun requestToken(
        nodeAddress: HttpUrl,
        alias: String,
        displayName: String,
        pin: String?,
    ): Token {
        require(alias.isNotBlank()) { "alias is blank." }
        require(displayName.isNotBlank()) { "displayName is blank." }
        val response = client.await {
            val request = RequestTokenRequest(
                display_name = displayName,
                conference_extension = alias
            )
            val requestBody = Json.encodeToRequestBody(request)
            post(requestBody)
            url(nodeAddress.resolve("api/client/v2/conferences/$alias/request_token")!!)
            pin?.let { header("pin", it.trim()) }
        }
        val (result) = response.use {
            when (it.code) {
                200 -> Json.decodeFromResponseBody<Box<Token>>(it.body!!)
                403 -> when (it.request.header("pin")) {
                    null -> {
                        val (r) = Json.decodeFromResponseBody<Box<RequestToken403Response>>(it.body!!)
                        throw RequiredPinException(r.guest_pin == "required")
                    }
                    else -> {
                        val (message) = Json.decodeFromResponseBody<Box<String>>(it.body!!)
                        throw InvalidPinException(message)
                    }
                }
                404 -> try {
                    val (message) = Json.decodeFromResponseBody<Box<String>>(it.body!!)
                    throw NoSuchConferenceException(message)
                } catch (e: SerializationException) {
                    throw NoSuchNodeException()
                }
                else -> throw IllegalStateException()
            }
        }
        return result
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
