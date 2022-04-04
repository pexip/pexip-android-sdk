package com.pexip.sdk.api.infinity.internal

import com.pexip.sdk.api.Call
import com.pexip.sdk.api.infinity.InfinityService
import com.pexip.sdk.api.infinity.NoSuchNodeException
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Response
import java.net.URL

internal class RealRequestBuilder(
    private val client: OkHttpClient,
    private val json: Json,
    private val node: URL,
) : InfinityService.RequestBuilder {

    override fun status(): Call<Boolean> = RealCall(
        call = client.newCall {
            get()
            url(node, "status")
        },
        mapper = ::parseStatus
    )

    override fun conference(conferenceAlias: String): InfinityService.ConferenceStep {
        require(conferenceAlias.isNotBlank()) { "conferenceAlias is blank." }
        return RealConferenceStep(client, json, node, conferenceAlias)
    }

    private fun parseStatus(response: Response) = when (response.code) {
        200 -> true
        503 -> false
        404 -> throw NoSuchNodeException()
        else -> throw IllegalStateException()
    }
}
