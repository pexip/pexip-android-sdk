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

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.fail
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import java.util.concurrent.atomic.AtomicInteger
import kotlin.random.Random
import kotlin.test.BeforeTest
import kotlin.test.Test

class TokenStoreTest {

    private lateinit var token: Token
    private lateinit var store: TokenStore

    @BeforeTest
    fun setUp() {
        token = Random.nextFakeToken()
        store = TokenStore(token)
    }

    @Test
    fun `token returns the correct value`() = runTest {
        store.token.test {
            assertThat(awaitItem(), "token").isEqualTo(token)
            repeat(10) {
                val token = store.update { Random.nextFakeToken() }
                assertThat(awaitItem(), "token").isEqualTo(token)
            }
        }
    }

    @Test
    fun `refreshes the token every so often`() = runTest {
        val tokensIndex = AtomicInteger()
        val tokens = List(10) { Random.nextFakeToken() }
        val store = TokenStore(tokens.first())
        val releaseTokenDeferred = CompletableDeferred<Token>()
        val job = store.refreshTokenIn(
            scope = this,
            refreshToken = {
                assertThat(it, "token").isEqualTo(tokens[tokensIndex.get()])
                tokens[tokensIndex.incrementAndGet()]
            },
            releaseToken = releaseTokenDeferred::complete,
            onFailure = { throw it },
        )
        repeat(tokens.size - 2) {
            val token = tokens[it + 1]
            testScheduler.advanceTimeBy(token.expires / 4)
            assertThat(store.token.value, "token").isEqualTo(token)
            testScheduler.advanceTimeBy(token.expires / 4)
            yield()
            val newToken = tokens[it + 2]
            assertThat(store.token.value, "token").isEqualTo(newToken)
        }
        job.cancelAndJoin()
        assertThat(releaseTokenDeferred.await(), "releaseTokenDeferred")
            .isEqualTo(tokens[tokensIndex.get()])
    }

    @Test
    fun `invokes the callback on failures`() = runTest {
        val t = Throwable()
        val token = Random.nextFakeToken()
        val store = TokenStore(token)
        val tDeferred = CompletableDeferred<Throwable>()
        val releaseTokenDeferred = CompletableDeferred<Token>()
        val job = store.refreshTokenIn(
            scope = this,
            refreshToken = {
                assertThat(it, "token").isEqualTo(token)
                throw t
            },
            releaseToken = releaseTokenDeferred::complete,
            onFailure = tDeferred::complete,
        )
        assertThat(tDeferred.await(), "t").isEqualTo(t)
        job.cancelAndJoin()
        assertThat(releaseTokenDeferred.await(), "releaseTokenDeferred").isEqualTo(token)
    }

    @Test
    fun `ignores releaseToken failures`() = runTest {
        val t = Throwable()
        val token = Random.nextFakeToken()
        val store = TokenStore(token)
        val tDeferred = CompletableDeferred<Token>()
        val job = store.refreshTokenIn(
            scope = this,
            refreshToken = { awaitCancellation() },
            releaseToken = {
                tDeferred.complete(it)
                throw t
            },
            onFailure = { fail("onFailure should not be called.") },
        )
        job.cancelAndJoin()
        assertThat(tDeferred.await(), "token").assertThat(token)
    }
}
