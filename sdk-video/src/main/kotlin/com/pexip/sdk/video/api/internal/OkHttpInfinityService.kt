package com.pexip.sdk.video.api.internal

import com.pexip.sdk.video.InvalidTokenException
import com.pexip.sdk.video.NoSuchConferenceException
import com.pexip.sdk.video.NoSuchNodeException
import com.pexip.sdk.video.Token
import com.pexip.sdk.video.api.InfinityService
import com.pexip.sdk.video.internal.Box
import com.pexip.sdk.video.internal.Json
import com.pexip.sdk.video.internal.await
import com.pexip.sdk.video.internal.decodeFromResponseBody
import kotlinx.serialization.SerializationException
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.internal.EMPTY_REQUEST

internal class OkHttpInfinityService(private val client: OkHttpClient) : InfinityService {

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
}
