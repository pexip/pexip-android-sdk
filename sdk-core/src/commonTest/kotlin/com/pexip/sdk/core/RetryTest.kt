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
package com.pexip.sdk.core

import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import com.pexip.sdk.core.internal.IOException
import kotlinx.coroutines.test.runTest
import kotlin.random.Random
import kotlin.test.BeforeTest
import kotlin.test.Test

class RetryTest {

    private lateinit var attempt: AtomicInteger

    @BeforeTest
    fun setUp() {
        attempt = AtomicInteger()
    }

    @Test
    fun `does n calls and rethrows the IOException`() = runTest {
        val times = Random.nextInt(10, 20)
        assertFailure {
            retry<Unit>(times) {
                attempt.incrementAndGet()
                throw IOException()
            }
        }.isInstanceOf<IOException>()
        assertThat(attempt.get()).isEqualTo(times)
    }

    @Test
    fun `rethrows immediately on non-IOException`() = runTest {
        val times = Random.nextInt(10, 20)
        val failure = Random.nextInt(2, times)
        val e = UnsupportedOperationException()
        assertFailure {
            retry<Unit>(times) {
                throw when (attempt.incrementAndGet()) {
                    failure -> e
                    else -> IOException()
                }
            }
        }.isEqualTo(e)
        assertThat(attempt.get()).isEqualTo(failure)
    }

    @Test
    fun `returns the value after failing several times`() = runTest {
        val times = Random.nextInt(10, 20)
        val success = Random.nextInt(2, times)
        val result = Random.nextInt()
        val actual = retry(times) {
            when (attempt.incrementAndGet()) {
                success -> result
                else -> throw IOException()
            }
        }
        assertThat(actual).isEqualTo(result)
        assertThat(attempt.get()).isEqualTo(success)
    }
}
