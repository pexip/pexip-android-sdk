package com.pexip.sdk.video.internal

import com.pexip.sdk.video.nextToken
import java.util.concurrent.ExecutorService
import kotlin.random.Random
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.minutes

internal class TokenStoreTest {

    private lateinit var executor: ExecutorService
    private lateinit var store: TokenStore

    @BeforeTest
    fun setUp() {
        store = TokenStore(Random.nextToken(), 2.minutes)
    }

    @Test
    fun `updates token`() {
        val token = Random.nextToken()
        store.token = token
        assertEquals(token, store.token)
    }
}
