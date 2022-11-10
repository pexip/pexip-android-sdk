package com.pexip.sdk.api.infinity.internal

import com.pexip.sdk.api.Call
import com.pexip.sdk.api.infinity.InfinityService
import com.pexip.sdk.api.infinity.NoSuchNodeException
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.net.URL

internal class RealRequestBuilder(
    private val client: OkHttpClient,
    private val json: Json,
    private val node: URL,
) : InfinityService.RequestBuilder {

    override fun status(): Call<Boolean> = RealCall(
        client = client,
        request = Request.Builder()
            .get()
            .url(node) { addPathSegment("status") }
            .build(),
        mapper = ::parseStatus
    )

    override fun conference(conferenceAlias: String): InfinityService.ConferenceStep {
        require(conferenceAlias.isNotBlank()) { "conferenceAlias is blank." }
        return RealConferenceStep(
            client = client,
            json = json,
            node = node,
            conferenceAlias = conferenceAlias
        )
    }

    override fun registration(deviceAlias: String): InfinityService.RegistrationStep {
        require(deviceAlias.isNotBlank()) { "registrationAlias is blank." }
        return RealRegistrationStep(
            client = client,
            json = json,
            node = node,
            deviceAlias = deviceAlias
        )
    }

    private fun parseStatus(response: Response) = when (response.code) {
        200 -> true
        503 -> false
        404 -> throw NoSuchNodeException()
        else -> throw IllegalStateException()
    }
}
