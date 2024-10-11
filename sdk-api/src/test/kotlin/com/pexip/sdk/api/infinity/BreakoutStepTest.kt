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
package com.pexip.sdk.api.infinity

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.pexip.sdk.infinity.BreakoutId
import com.pexip.sdk.infinity.test.nextBreakoutId
import com.pexip.sdk.infinity.test.nextString
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl
import org.junit.Rule
import kotlin.properties.Delegates
import kotlin.random.Random
import kotlin.test.BeforeTest
import kotlin.test.Test

class BreakoutStepTest {

    @get:Rule
    val rule = SecureMockWebServerRule()

    private val server get() = rule.server
    private val client get() = rule.client

    private lateinit var node: HttpUrl
    private lateinit var conferenceAlias: String
    private lateinit var json: Json
    private lateinit var token: Token
    private lateinit var step: InfinityService.BreakoutStep

    private var breakoutId: BreakoutId by Delegates.notNull()

    @BeforeTest
    fun setUp() {
        node = server.url("/")
        conferenceAlias = Random.nextString()
        breakoutId = Random.nextBreakoutId()
        json = InfinityService.Json
        token = Random.nextFakeToken()
        step = InfinityService.create(client, json)
            .newRequest(node)
            .conference(conferenceAlias)
            .breakout(breakoutId)
    }

    @Test
    fun `breakoutId returns the correct value`() {
        assertThat(step::breakoutId).isEqualTo(breakoutId)
    }
}
