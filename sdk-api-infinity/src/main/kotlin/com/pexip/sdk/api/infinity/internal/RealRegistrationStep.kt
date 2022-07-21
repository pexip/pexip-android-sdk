package com.pexip.sdk.api.infinity.internal

import com.pexip.sdk.api.Call
import com.pexip.sdk.api.infinity.InfinityService
import com.pexip.sdk.api.infinity.InvalidTokenException
import com.pexip.sdk.api.infinity.NoSuchConferenceException
import com.pexip.sdk.api.infinity.NoSuchNodeException
import com.pexip.sdk.api.infinity.NoSuchRegistrationException
import com.pexip.sdk.api.infinity.RefreshRegistrationTokenResponse
import com.pexip.sdk.api.infinity.RequestRegistrationTokenResponse
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.internal.EMPTY_REQUEST
import okio.ByteString.Companion.encodeUtf8

internal class RealRegistrationStep(
    private val client: OkHttpClient,
    private val json: Json,
    private val url: HttpUrl,
) : InfinityService.RegistrationStep {

    override fun requestToken(
        username: String,
        password: String,
    ): Call<RequestRegistrationTokenResponse> {
        require(username.isNotBlank()) { "username is blank." }
        require(password.isNotBlank()) { "password is blank." }
        val base64 = "$username:$password".encodeUtf8().base64Url()
        return RealCall(
            client = client,
            request = Request.Builder()
                .post(EMPTY_REQUEST)
                .url(HttpUrl(url) { addPathSegment("request_token") })
                .header("Authorization", "x-pexip-basic $base64")
                .build(),
            mapper = ::parseRequestToken
        )
    }

    override fun refreshToken(token: String): Call<RefreshRegistrationTokenResponse> {
        require(token.isNotBlank()) { "token is blank." }
        return RealCall(
            client = client,
            request = Request.Builder()
                .post(EMPTY_REQUEST)
                .url(HttpUrl(url) { addPathSegment("refresh_token") })
                .header("token", token)
                .build(),
            mapper = ::parseRefreshToken
        )
    }

    override fun releaseToken(token: String): Call<Boolean> {
        require(token.isNotBlank()) { "token is blank." }
        return RealCall(
            client = client,
            request = Request.Builder()
                .post(EMPTY_REQUEST)
                .url(HttpUrl(url) { addPathSegment("release_token") })
                .header("token", token)
                .build(),
            mapper = ::parseReleaseToken
        )
    }

    private fun parseRequestToken(response: Response) = when (response.code) {
        200 -> json.decodeFromResponseBody(
            deserializer = RequestRegistrationTokenResponseSerializer,
            body = response.body!!
        )
        401 -> response.parse401()
        403 -> response.parse403()
        404 -> response.parse404()
        else -> throw IllegalStateException()
    }

    private fun parseRefreshToken(response: Response) = when (response.code) {
        200 -> json.decodeFromResponseBody(
            deserializer = RefreshRegistrationTokenResponseSerializer,
            body = response.body!!
        )
        401 -> response.parse401()
        403 -> response.parse403()
        404 -> response.parse404()
        else -> throw IllegalStateException()
    }

    private fun parseReleaseToken(response: Response) = when (response.code) {
        200 -> json.decodeFromResponseBody(BooleanSerializer, response.body!!)
        401 -> response.parse401()
        403 -> response.parse403()
        404 -> response.parse404()
        else -> throw IllegalStateException()
    }

    private fun Response.parse401(): Nothing {
        throw NoSuchRegistrationException(body?.string())
    }

    private fun Response.parse403(): Nothing {
        val message = json.decodeFromResponseBody(StringSerializer, body!!)
        throw InvalidTokenException(message)
    }

    private fun Response.parse404(): Nothing = try {
        val message = json.decodeFromResponseBody(StringSerializer, body!!)
        throw NoSuchConferenceException(message)
    } catch (e: SerializationException) {
        throw NoSuchNodeException()
    }
}
