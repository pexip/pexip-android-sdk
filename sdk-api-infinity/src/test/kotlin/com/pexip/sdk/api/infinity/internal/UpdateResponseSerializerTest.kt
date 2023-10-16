/*
 * Copyright 2023 Pexip AS
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
import com.pexip.sdk.api.infinity.UpdateResponse
import kotlinx.serialization.json.Json
import okio.BufferedSource
import okio.FileSystem
import okio.Path.Companion.toPath
import kotlin.test.Test

class UpdateResponseSerializerTest {

    @Test
    fun `correctly deserializes update response`() {
        val json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }
        tableOf("filename", "sdp")
            .row("update_response.json", "uqbNGpd1u4")
            .row("update_response_direct_media.json", "D7ZanzX2QK")
            .forAll { filename, sdp ->
                val content = FileSystem.RESOURCES.read(filename.toPath(), BufferedSource::readUtf8)
                val response = json.decodeFromString(UpdateResponseSerializer, content)
                assertThat(UpdateResponse(sdp), "sdp").isEqualTo(response)
            }
    }
}
