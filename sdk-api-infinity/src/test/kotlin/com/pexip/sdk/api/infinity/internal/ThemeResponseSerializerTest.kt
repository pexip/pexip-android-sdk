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
import assertk.assertions.containsOnly
import com.pexip.sdk.api.infinity.BackgroundResponse
import com.pexip.sdk.api.infinity.ElementResponse
import com.pexip.sdk.api.infinity.InfinityService
import com.pexip.sdk.api.infinity.SplashScreenResponse
import kotlinx.serialization.json.Json
import okio.BufferedSource
import okio.FileSystem
import okio.Path.Companion.toPath
import kotlin.test.BeforeTest
import kotlin.test.Test

class ThemeResponseSerializerTest {

    private lateinit var json: Json

    @BeforeTest
    fun setUp() {
        json = InfinityService.Json
    }

    @Test
    fun `correctly deserializes the response`() {
        val content = FileSystem.RESOURCES.read("theme.json".toPath(), BufferedSource::readUtf8)
        val actual = json.decodeFromString(ThemeSerializer, content)
        assertThat(actual, "response").containsOnly(
            "direct_media_welcome" to SplashScreenResponse(
                background = BackgroundResponse("background.jpg"),
                elements = listOf(
                    ElementResponse.Text(
                        color = 4294967295,
                        text = "Welcome",
                    ),
                ),
            ),
            "direct_media_waiting_for_host" to SplashScreenResponse(
                background = BackgroundResponse("background.jpg"),
                elements = listOf(
                    ElementResponse.Text(
                        color = 4294967295,
                        text = "Waiting for the host...",
                    ),
                ),
            ),
            "direct_media_other_participants_audio_only" to SplashScreenResponse(
                background = BackgroundResponse("background.jpg"),
                elements = listOf(
                    ElementResponse.Text(
                        color = 4294967295,
                        text = "The other participants\n are audio only",
                    ),
                ),
            ),
            "direct_media_escalate" to SplashScreenResponse(
                background = BackgroundResponse("background.jpg"),
                elements = listOf(
                    ElementResponse.Text(
                        color = 4294967295,
                        text = "You are about to be transferred\ninto a multi-party conference",
                    ),
                ),
            ),
            "direct_media_deescalate" to SplashScreenResponse(
                background = BackgroundResponse("background.jpg"),
                elements = listOf(
                    ElementResponse.Text(
                        color = 4294967295,
                        text = "You are about to be transferred\ninto a one-to-one call",
                    ),
                ),
            ),
        )
    }
}
