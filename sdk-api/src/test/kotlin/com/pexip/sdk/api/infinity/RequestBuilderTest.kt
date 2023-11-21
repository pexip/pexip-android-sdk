/*
 * Copyright 2022 Pexip AS
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

import okhttp3.mockwebserver.MockWebServer
import org.junit.Rule
import java.net.URL
import kotlin.properties.Delegates
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class RequestBuilderTest {

    @get:Rule
    val server = MockWebServer()

    private lateinit var builder: InfinityService.RequestBuilder

    private var node: URL by Delegates.notNull()

    @BeforeTest
    fun setUp() {
        node = server.url("/").toUrl()
        val service = InfinityService.create()
        builder = service.newRequest(node)
    }

    @Test
    fun `status throws IllegalStateException`() {
        server.enqueue { setResponseCode(500) }
        assertFailsWith<IllegalStateException> { builder.status().execute() }
        server.verifyStatus()
    }

    @Test
    fun `status throws NoSuchNodeException`() {
        server.enqueue { setResponseCode(404) }
        assertFailsWith<NoSuchNodeException> { builder.status().execute() }
        server.verifyStatus()
    }

    @Test
    fun `status returns false`() {
        server.enqueue { setResponseCode(503) }
        assertFalse(builder.status().execute())
        server.verifyStatus()
    }

    @Test
    fun `status returns true`() {
        server.enqueue { setResponseCode(200) }
        assertTrue(builder.status().execute())
        server.verifyStatus()
    }

    private fun MockWebServer.verifyStatus() = takeRequest {
        assertRequestUrl(node) { addPathSegments("api/client/v2/status") }
        assertGet()
    }
}
