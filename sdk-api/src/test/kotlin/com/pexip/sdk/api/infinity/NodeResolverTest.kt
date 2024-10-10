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
@file:Suppress("DEPRECATION")

package com.pexip.sdk.api.infinity

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.net.URL
import kotlin.test.BeforeTest
import kotlin.test.Test

@RunWith(Parameterized::class)
internal class NodeResolverTest(private val host: String, private val url: String?) {

    private lateinit var resolver: NodeResolver

    @BeforeTest
    fun setUp() {
        resolver = NodeResolver.create()
    }

    @Test
    fun `onSuccess is called`() = runTest {
        assertThat(resolver.resolve(host).await()).isEqualTo(listOfNotNull(url?.let(::URL)))
    }

    companion object {

        @JvmStatic
        @get:Parameterized.Parameters(name = "{0}")
        val testCases = listOf(
            // SRV record
            arrayOf("pexip.com", "https://pexipdemo.com"),
            // A record
            arrayOf("google.com", "https://google.com"),
            // Not a real domain
            arrayOf("b.c", null),
        )
    }
}
