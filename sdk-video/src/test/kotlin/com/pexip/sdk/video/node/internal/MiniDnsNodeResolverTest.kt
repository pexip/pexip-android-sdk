package com.pexip.sdk.video.node.internal

import kotlinx.coroutines.runBlocking
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@RunWith(RobolectricTestRunner::class)
class MiniDnsNodeResolverTest {

    private lateinit var resolver: NodeResolver

    @BeforeTest
    fun setUp() {
        resolver = MiniDnsNodeResolver()
    }

    @Test
    fun `returns DNS SRV record`() = runBlocking {
        assertEquals("pexipdemo.com", resolver.resolve("dzmitry.rymarau@pexip.com"))
    }

    @Test
    fun `returns A record`() = runBlocking {
        assertEquals("93.184.216.34", resolver.resolve("me@example.com"))
    }

    @Test
    fun `throws NoSuchElementException`() = runBlocking<Unit> {
        assertFailsWith<NoSuchElementException> { resolver.resolve("a@b.c") }
    }
}
