/*
 * Copyright 2022-2024 Pexip AS
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

import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.isTrue
import com.pexip.sdk.infinity.Node
import kotlinx.coroutines.test.runTest
import okhttp3.HttpUrl
import okhttp3.mockwebserver.MockWebServer
import org.junit.Rule
import kotlin.test.BeforeTest
import kotlin.test.Test

internal class RequestBuilderTest {

    @get:Rule
    val rule = SecureMockWebServerRule()

    private val server get() = rule.server
    private val client get() = rule.client

    private lateinit var node: HttpUrl
    private lateinit var builder: InfinityService.RequestBuilder

    @BeforeTest
    fun setUp() {
        node = server.url("/")
        builder = InfinityService.create(client).newRequest(node)
    }

    @Test
    fun `node returns the correct value`() {
        val expected = Node(node.host, node.port)
        assertThat(builder::node).isEqualTo(expected)
    }

    @Test
    fun `status throws IllegalStateException`() = runTest {
        server.enqueue { setResponseCode(500) }
        assertFailure { builder.status().await() }.isInstanceOf<IllegalStateException>()
        server.verifyStatus()
    }

    @Test
    fun `status throws NoSuchNodeException`() = runTest {
        server.enqueue { setResponseCode(404) }
        assertFailure { builder.status().await() }.isInstanceOf<NoSuchNodeException>()
        server.verifyStatus()
    }

    @Test
    fun `status returns false`() = runTest {
        server.enqueue { setResponseCode(503) }
        assertThat(builder.status().await(), "status").isFalse()
        server.verifyStatus()
    }

    @Test
    fun `status returns true`() = runTest {
        server.enqueue { setResponseCode(200) }
        assertThat(builder.status().await()).isTrue()
        server.verifyStatus()
    }

    private fun MockWebServer.verifyStatus() = takeRequest {
        assertRequestUrl(node) { addPathSegments("api/client/v2/status") }
        assertGet()
    }
}
