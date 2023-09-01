/*
 * Copyright 2022-2023 Pexip AS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.pexip.sdk.api.infinity.internal

import com.pexip.sdk.api.Call
import com.pexip.sdk.api.EventSourceFactory
import com.pexip.sdk.api.infinity.InfinityService
import com.pexip.sdk.api.infinity.InvalidTokenException
import com.pexip.sdk.api.infinity.NoSuchConferenceException
import com.pexip.sdk.api.infinity.NoSuchNodeException
import com.pexip.sdk.api.infinity.NoSuchRegistrationException
import com.pexip.sdk.api.infinity.RefreshRegistrationTokenResponse
import com.pexip.sdk.api.infinity.RegistrationResponse
import com.pexip.sdk.api.infinity.RequestRegistrationTokenResponse
import com.pexip.sdk.api.infinity.Token
import kotlinx.serialization.SerializationException
import okhttp3.Request
import okhttp3.Response
import okhttp3.internal.EMPTY_REQUEST
import okio.ByteString.Companion.encodeUtf8

internal class RegistrationStepImpl(
    override val requestBuilder: RequestBuilderImpl,
    private val deviceAlias: String,
) : InfinityService.RegistrationStep, RequestBuilderImplScope by requestBuilder {

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
                .url(node) {
                    registration(deviceAlias)
                    addPathSegment("request_token")
                }
                .header("Authorization", "x-pexip-basic $base64")
                .build(),
            mapper = ::parseRequestToken,
        )
    }

    override fun refreshToken(token: String): Call<RefreshRegistrationTokenResponse> {
        require(token.isNotBlank()) { "token is blank." }
        return RealCall(
            client = client,
            request = Request.Builder()
                .post(EMPTY_REQUEST)
                .url(node) {
                    registration(deviceAlias)
                    addPathSegment("refresh_token")
                }
                .header("token", token)
                .build(),
            mapper = ::parseRefreshToken,
        )
    }

    override fun refreshToken(token: Token): Call<RefreshRegistrationTokenResponse> =
        refreshToken(token.token)

    override fun releaseToken(token: String): Call<Boolean> {
        require(token.isNotBlank()) { "token is blank." }
        return RealCall(
            client = client,
            request = Request.Builder()
                .post(EMPTY_REQUEST)
                .url(node) {
                    registration(deviceAlias)
                    addPathSegment("release_token")
                }
                .header("token", token)
                .build(),
            mapper = ::parseReleaseToken,
        )
    }

    override fun releaseToken(token: Token): Call<Boolean> = releaseToken(token.token)

    override fun events(token: String): EventSourceFactory {
        require(token.isNotBlank()) { "token is blank." }
        return RealEventSourceFactory(
            client = client,
            request = Request.Builder()
                .get()
                .url(node) {
                    registration(deviceAlias)
                    addPathSegment("events")
                }
                .header("token", token)
                .build(),
            json = json,
        )
    }

    override fun events(token: Token): EventSourceFactory = events(token.token)

    override fun registrations(token: String, query: String): Call<List<RegistrationResponse>> {
        require(token.isNotBlank()) { "token is blank." }
        return RealCall(
            client = client,
            request = Request.Builder()
                .get()
                .url(node) {
                    registration()
                    if (query.isNotBlank()) {
                        addQueryParameter("q", query.trim())
                    }
                }
                .header("token", token)
                .build(),
            mapper = ::parseRegistrations,
        )
    }

    override fun registrations(token: Token, query: String): Call<List<RegistrationResponse>> =
        registrations(token.token, query)

    private fun parseRequestToken(response: Response) = when (response.code) {
        200 -> json.decodeFromResponseBody(
            deserializer = RequestRegistrationTokenResponseSerializer,
            body = response.body!!,
        )
        401 -> response.parse401()
        403 -> response.parse403()
        404 -> response.parse404()
        else -> throw IllegalStateException()
    }

    private fun parseRefreshToken(response: Response) = when (response.code) {
        200 -> json.decodeFromResponseBody(
            deserializer = RefreshRegistrationTokenResponseSerializer,
            body = response.body!!,
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

    private fun parseRegistrations(response: Response) = when (response.code) {
        200 -> json.decodeFromResponseBody(RegistrationResponseSerializer, response.body!!)
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
