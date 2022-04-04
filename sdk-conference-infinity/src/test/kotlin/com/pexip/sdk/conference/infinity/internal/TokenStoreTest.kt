package com.pexip.sdk.conference.infinity.internal

import kotlin.random.Random
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class TokenStoreTest {

    private lateinit var token: String
    private lateinit var store: TokenStore

    @BeforeTest
    fun setUp() {
        token = Random.nextString(8)
        store = RealTokenStore(token)
    }

    @Test
    fun `get() returns current token`() {
        assertEquals(token, store.get())
    }

    @Test
    fun `updateAndGet() returns updated token`() {
        val newToken = Random.nextString(8)
        assertEquals(
            expected = newToken,
            actual = store.updateAndGet {
                assertEquals(token, it)
                newToken
            }
        )
        assertEquals(newToken, store.get())
    }
}
