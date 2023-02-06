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
package com.pexip.sdk.conference.infinity.internal

import com.pexip.sdk.api.infinity.Token
import com.pexip.sdk.api.infinity.TokenStore
import kotlin.random.Random
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class TokenStoreTest {

    private lateinit var token: Token
    private lateinit var store: TokenStore

    @BeforeTest
    fun setUp() {
        token = Random.nextToken()
        store = TokenStore.create(token)
    }

    @Test
    fun `get() returns current token`() {
        assertEquals(token, store.get())
    }

    @Test
    fun `set() updates the token`() {
        val newToken = Random.nextToken()
        store.set(newToken)
        assertEquals(newToken, store.get())
    }
}
