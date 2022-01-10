package com.pexip.sdk.video.api.internal

import com.pexip.sdk.video.api.InfinityService
import com.pexip.sdk.video.api.PinRequirement
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient

internal class OkHttpInfinityService(private val client: OkHttpClient) : InfinityService {

    override suspend fun getPinRequirement(
        nodeAddress: String,
        conferenceAlias: String,
        displayName: String,
    ): PinRequirement {
        require(nodeAddress.isNotBlank()) { "nodeAddress is blank." }
        require(conferenceAlias.isNotBlank()) { "conferenceAlias is blank." }
        require(displayName.isNotBlank()) { "displayName is blank." }
        val response = client.await {
            val request = PinRequirementRequest(displayName)
            val requestBody = Json.encodeToRequestBody(request)
            post(requestBody)
            val url = nodeAddress
                .toHttpUrl()
                .resolve("api/client/v2/conferences/$conferenceAlias/request_token")!!
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

    internal companion object {

        val Json by lazy { Json { ignoreUnknownKeys = true } }
        val ApplicationJson by lazy { "application/json; charset=utf-8".toMediaType() }
    }
}
