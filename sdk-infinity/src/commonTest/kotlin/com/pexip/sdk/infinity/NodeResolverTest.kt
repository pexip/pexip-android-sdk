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
package com.pexip.sdk.infinity

import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.hasMessage
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.tableOf
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class NodeResolverTest {

    private lateinit var resolver: NodeResolver

    @BeforeTest
    fun setUp() {
        resolver = NodeResolver.create()
    }

    @Test
    fun `throws IllegalArgumentException if the host is invalid`() {
        tableOf("host", "message")
            .row("", "host is blank.") // An empty String
            .row(" ", "host is blank.") // A blank String
            .forAll(::`throws IllegalArgumentException if the host is invalid`)
    }

    @Test
    fun `returns a list of nodes`() {
        tableOf("host", "nodes")
            .row("pexip.com", listOf(Node("pexipdemo.com")))
            .row("google.com", listOf(Node("google.com")))
            .row("b.c", listOf())
            .forAll(::`returns a list of nodes`)
    }

    private fun `throws IllegalArgumentException if the host is invalid`(
        host: String,
        message: String,
    ) = runTest {
        assertFailure { resolver.resolve(host) }
            .isInstanceOf<IllegalArgumentException>()
            .hasMessage(message)
    }

    private fun `returns a list of nodes`(host: String, nodes: List<Node>) = runTest {
        assertThat(resolver.resolve(host), "nodes").isEqualTo(nodes)
    }
}
