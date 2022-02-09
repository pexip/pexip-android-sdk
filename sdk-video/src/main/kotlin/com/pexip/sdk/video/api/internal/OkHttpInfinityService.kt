package com.pexip.sdk.video.api.internal

import com.pexip.sdk.video.api.InfinityService
import com.pexip.sdk.video.api.InvalidPinException
import com.pexip.sdk.video.api.InvalidTokenException
import com.pexip.sdk.video.api.NoSuchConferenceException
import com.pexip.sdk.video.api.NoSuchNodeException
import com.pexip.sdk.video.api.RequiredPinException
import com.pexip.sdk.video.api.Token
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.internal.EMPTY_REQUEST

internal class OkHttpInfinityService(private val client: OkHttpClient) : InfinityService {

    override suspend fun requestToken(
        nodeAddress: HttpUrl,
        alias: String,
        displayName: String,
        pin: String?,
    ): Token {
        require(alias.isNotBlank()) { "alias is blank." }
        require(displayName.isNotBlank()) { "displayName is blank." }
        val response = client.await {
            val request = RequestTokenRequest(displayName)
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

    override suspend fun refreshToken(nodeAddress: HttpUrl, alias: String, token: String): Token {
        require(alias.isNotBlank()) { "alias is blank." }
        require(token.isNotBlank()) { "token is blank." }
        val response = client.await {
            post(EMPTY_REQUEST)
            url(nodeAddress.resolve("api/client/v2/conferences/$alias/refresh_token")!!)
            header("token", token)
        }
        val (result) = response.use {
            when (it.code) {
                200 -> Json.decodeFromResponseBody<Box<Token>>(it.body!!)
                403 -> {
                    val (message) = Json.decodeFromResponseBody<Box<String>>(it.body!!)
                    throw InvalidTokenException(message)
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

    override suspend fun releaseToken(nodeAddress: HttpUrl, alias: String, token: String) {
        require(alias.isNotBlank()) { "alias is blank." }
        require(token.isNotBlank()) { "token is blank." }
        val response = client.await {
            post(EMPTY_REQUEST)
            url(nodeAddress.resolve("api/client/v2/conferences/$alias/release_token")!!)
            header("token", token)
        }
        response.use {
            when (it.code) {
                200, 403 -> Unit
                404 -> try {
                    val (message) = Json.decodeFromResponseBody<Box<String>>(it.body!!)
                    throw NoSuchConferenceException(message)
                } catch (e: SerializationException) {
                    throw NoSuchNodeException()
                }
                else -> throw IllegalStateException()
            }
        }
    }

    companion object {

        val Json by lazy { Json { ignoreUnknownKeys = true } }
    }
}
