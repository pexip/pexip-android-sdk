package com.pexip.sdk.registration.infinity.internal

import com.pexip.sdk.api.Call
import com.pexip.sdk.api.EventSourceFactory
import com.pexip.sdk.api.infinity.InfinityService
import com.pexip.sdk.api.infinity.RefreshRegistrationTokenResponse
import com.pexip.sdk.api.infinity.RegistrationResponse
import com.pexip.sdk.api.infinity.RequestRegistrationTokenResponse
import com.pexip.sdk.api.infinity.Token

internal abstract class TestRegistrationStep : InfinityService.RegistrationStep {

    override fun requestToken(
        username: String,
        password: String,
    ): Call<RequestRegistrationTokenResponse> = TODO()

    override fun refreshToken(token: String): Call<RefreshRegistrationTokenResponse> =
        TODO()

    override fun refreshToken(token: Token): Call<RefreshRegistrationTokenResponse> =
        refreshToken(token.token)

    override fun releaseToken(token: String): Call<Boolean> = TODO()

    override fun releaseToken(token: Token): Call<Boolean> = releaseToken(token.token)

    override fun events(token: String): EventSourceFactory = TODO()

    override fun events(token: Token): EventSourceFactory = events(token.token)

    override fun registrations(token: String, query: String): Call<List<RegistrationResponse>> =
        TODO()

    override fun registrations(token: Token, query: String): Call<List<RegistrationResponse>> =
        registrations(token.token, query)
}
