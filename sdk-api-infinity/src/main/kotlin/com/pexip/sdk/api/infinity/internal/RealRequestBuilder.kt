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
import com.pexip.sdk.api.infinity.InfinityService
import com.pexip.sdk.api.infinity.NoSuchNodeException
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

internal class RealRequestBuilder(
    private val client: OkHttpClient,
    private val json: Json,
    private val node: HttpUrl,
) : InfinityService.RequestBuilder {

    override fun status(): Call<Boolean> = RealCall(
        client = client,
        request = Request.Builder()
            .get()
            .url(node) { addPathSegment("status") }
            .build(),
        mapper = ::parseStatus,
    )

    override fun conference(conferenceAlias: String): InfinityService.ConferenceStep {
        require(conferenceAlias.isNotBlank()) { "conferenceAlias is blank." }
        return RealConferenceStep(
            client = client,
            json = json,
            node = node,
            conferenceAlias = conferenceAlias,
        )
    }

    override fun registration(deviceAlias: String): InfinityService.RegistrationStep {
        require(deviceAlias.isNotBlank()) { "registrationAlias is blank." }
        return RealRegistrationStep(
            client = client,
            json = json,
            node = node,
            deviceAlias = deviceAlias,
        )
    }

    private fun parseStatus(response: Response) = when (response.code) {
        200 -> true
        503 -> false
        404 -> throw NoSuchNodeException()
        else -> throw IllegalStateException()
    }
}
