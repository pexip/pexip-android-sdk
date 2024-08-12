/*
 * Copyright 2024 Pexip AS
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

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.tableOf
import com.pexip.sdk.api.infinity.InfinityService
import com.pexip.sdk.api.infinity.RequestTokenResponse
import com.pexip.sdk.api.infinity.VersionResponse
import com.pexip.sdk.api.infinity.readUtf8
import com.pexip.sdk.infinity.ParticipantId
import com.pexip.sdk.infinity.ServiceType
import kotlinx.serialization.json.Json
import okio.FileSystem
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class RequestTokenResponseSerializerTest {

    private lateinit var json: Json

    @BeforeTest
    fun setUp() {
        json = InfinityService.Json
    }

    @Test
    fun `correctly deserializes RequestTokenResponse`() {
        tableOf("json", "response")
            .row(
                val1 = "request_token_v35.json",
                val2 = RequestTokenResponse(
                    token = "stK5GSZbt0KzXNGZG8VuSpEHrfGlWLinudoy7uMXV6awryupbBgY5i8ltpsTmGFJ_heiFjsxoTho6ystEDpJ8yASEqif-YfyUyxsvzpxWcYwSRCdo1J8nBU2FuoeoL3MY2WtEtOzbnQD8GCeU2T9ha2Bnvu_iFu6q3dS6iirOBoZ7SIxndrpB4z6zjxENhvSWYkaE6eS3oIZVg2hk0rDNykDt3OHPmBIu6hgQXXORYHUQrs6d1g=",
                    expires = 2.minutes,
                    conferenceName = "example",
                    participantId = ParticipantId("f22f8f50-0d85-47ea-bbf7-97f4eaa39f53"),
                    participantName = "George",
                    version = VersionResponse(
                        versionId = "35",
                        pseudoVersion = "77524.0.0",
                    ),
                    chatEnabled = true,
                    guestsCanPresent = true,
                    serviceType = ServiceType.UNKNOWN,
                    directMedia = true,
                    dataChannelId = 3,
                    clientStatsUpdateInterval = 5.seconds,
                ),
            )
            .row(
                val1 = "request_token_v36.json",
                val2 = RequestTokenResponse(
                    token = "yaanKhAlQFdKJSLRHhxzNHulGdyFzmjUyescAVAdRK3kiC3uj5G2puAicBq3uG_Et4kHkNF-5jP7V-Ku4-XSahWZLHjIMWYvyzXb1EIzqWW_297MPZF7FEsEHKTM7yAnr9-Hdv-C6qWeqZxQe6eEw8vY7x8QbdphXH3xpOjTnKel3AP6KXy1QkhQE329_deztUWEjE7ZGZwpmUgIjv3eU1bVmDdWyp3K29wOK30kAGACkRptZqPh0Q1R9mhGo9yPzhg=",
                    expires = 2.minutes,
                    conferenceName = "example",
                    participantId = ParticipantId("614221f4-3a5c-4d1f-8df6-3ae27188df54"),
                    participantName = "George",
                    version = VersionResponse(
                        versionId = "36",
                        pseudoVersion = "79019.0.0",
                    ),
                    chatEnabled = true,
                    guestsCanPresent = true,
                    serviceType = ServiceType.UNKNOWN,
                    directMedia = true,
                    dataChannelId = 3,
                    clientStatsUpdateInterval = 4411.068996234768.milliseconds,
                ),
            )
            .forAll { filename, expected ->
                val data = FileSystem.RESOURCES.readUtf8(filename)
                assertThat(json.decodeFromString(RequestTokenResponseSerializer, data))
                    .isEqualTo(expected)
            }
    }
}
