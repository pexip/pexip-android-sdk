package com.pexip.sdk.video.api.internal

import android.util.Log
import com.pexip.sdk.video.api.InfinityService
import com.pexip.sdk.video.api.InvalidPinException
import com.pexip.sdk.video.api.NoSuchConferenceException
import com.pexip.sdk.video.api.NoSuchNodeException
import com.pexip.sdk.video.api.RequiredPinException
import com.pexip.sdk.video.api.Token
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

internal class OkHttpInfinityService(private val client: OkHttpClient) : InfinityService {

    constructor() : this(OkHttpClient)

    override suspend fun isInMaintenanceMode(nodeAddress: String): Boolean {
        require(nodeAddress.isNotBlank()) { "nodeAddress is blank." }
        val response = client.await {
            get()
            val url = nodeAddress
                .toHttpUrl()
                .resolve("api/client/v2/status")!!
            url(url)
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
        nodeAddress: String,
        alias: String,
        displayName: String,
        pin: String?,
    ): Token {
        require(nodeAddress.isNotBlank()) { "nodeAddress is blank." }
        require(alias.isNotBlank()) { "alias is blank." }
        require(displayName.isNotBlank()) { "displayName is blank." }
        val response = client.await {
            val request = RequestTokenRequest(displayName)
            val requestBody = Json.encodeToRequestBody(request)
            post(requestBody)
            val url = nodeAddress
                .toHttpUrl()
                .resolve("api/client/v2/conferences/$alias/request_token")!!
            url(url)
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
        return Token(
            token = result.token,
            expires = result.expires
        )
    }

    companion object {

        val OkHttpClient by lazy {
            OkHttpClient {
                val interceptor = HttpLoggingInterceptor { Log.d("OkHttpInfinityService", it) }
                interceptor.level = HttpLoggingInterceptor.Level.BODY
                addInterceptor(interceptor)
            }
        }
        val Json by lazy { Json { ignoreUnknownKeys = true } }
    }
}
