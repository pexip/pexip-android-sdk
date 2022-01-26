package com.pexip.sdk.video.node

import kotlinx.coroutines.runBlocking
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@RunWith(RobolectricTestRunner::class)
class NodeResolverTest {

    private lateinit var resolver: NodeResolver

    @BeforeTest
    fun setUp() {
        resolver = NodeResolver
    }

    @Test
    fun `returns DNS SRV record`() = runBlocking {
        assertEquals(
            expected = "https://pexipdemo.com",
            actual = resolver.resolve("pexip.com")
        )
    }

    @Test
    fun `returns A record`() = runBlocking {
        assertEquals(
            expected = "https://93.184.216.34",
            actual = resolver.resolve("example.com")
        )
    }

    @Test
    fun `throws NoSuchElementException`() = runBlocking<Unit> {
        assertFailsWith<NoSuchElementException> { resolver.resolve("b.c") }
    }
}
