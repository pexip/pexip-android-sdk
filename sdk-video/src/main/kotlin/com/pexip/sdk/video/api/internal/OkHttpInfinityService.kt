package com.pexip.sdk.video.api.internal

import android.util.Log
import com.pexip.sdk.video.api.InfinityService
import com.pexip.sdk.video.api.PinRequirement
import com.pexip.sdk.video.api.Token
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

    override suspend fun getPinRequirement(
        nodeAddress: String,
        alias: String,
        displayName: String,
    ): PinRequirement {
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
        }
        val (result) = response.use {
            when (it.code) {
                200 -> Json.decodeFromResponseBody<Box<RequestToken200Response>>(it.body!!)
                403 -> Json.decodeFromResponseBody<Box<RequestToken403Response>>(it.body!!)
                404 -> throw NoSuchConferenceException()
                else -> throw IllegalStateException()
            }
        }
        return when (result) {
            is RequestToken200Response -> PinRequirement.None(
                token = result.token,
                expires = result.expires
            )
            is RequestToken403Response -> PinRequirement.Some(result.guest_pin == "required")
        }
    }

    override suspend fun requestToken(
        nodeAddress: String,
        alias: String,
        displayName: String,
        pin: String,
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
            header("pin", pin)
        }
        val (result) = response.use {
            when (it.code) {
                200 -> Json.decodeFromResponseBody<Box<RequestToken200Response>>(it.body!!)
                403 -> throw InvalidPinException()
                404 -> throw NoSuchConferenceException()
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
