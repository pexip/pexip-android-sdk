package com.pexip.sdk.video.api.internal

import com.pexip.sdk.video.api.InfinityService
import com.pexip.sdk.video.api.InvalidPinException
import com.pexip.sdk.video.api.NoSuchConferenceException
import com.pexip.sdk.video.api.NoSuchNodeException
import com.pexip.sdk.video.api.RequiredPinException
import com.pexip.sdk.video.api.Token
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl
import okhttp3.OkHttpClient

internal class OkHttpInfinityService(private val client: OkHttpClient) : InfinityService {

    override suspend fun isInMaintenanceMode(nodeAddress: HttpUrl): Boolean {
        val response = client.await {
            get()
            url(nodeAddress.resolve("api/client/v2/status")!!)
        }
        return response.use {
            when (it.code) {
                200 -> false
                404 -> throw NoSuchNodeException()
                503 -> true
                else -> throw IllegalStateException()
            }
        }
    }

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
        val (token) = response.use {
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
        return token
    }

    companion object {

        val Json by lazy { Json { ignoreUnknownKeys = true } }
    }
}
