package com.pexip.sdk.api.infinity.internal

import com.pexip.sdk.api.infinity.InfinityService
import com.pexip.sdk.api.infinity.InfinityService.RequestBuilder
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import java.net.URL

internal class RealInfinityService(
    private val client: OkHttpClient,
    private val json: Json = Json { ignoreUnknownKeys = true },
) : InfinityService {

    override fun newRequest(node: URL): RequestBuilder = RealRequestBuilder(
        client = client,
        json = json,
        url = HttpUrl(node) {
            addPathSegment("api")
            addPathSegment("client")
            addPathSegment("v2")
        }
    )
}
