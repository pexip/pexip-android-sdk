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
