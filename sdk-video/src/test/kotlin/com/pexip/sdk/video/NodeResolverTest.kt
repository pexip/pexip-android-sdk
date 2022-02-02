package com.pexip.sdk.video

import kotlinx.coroutines.runBlocking
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@RunWith(RobolectricTestRunner::class)
class NodeResolverTest {

    private lateinit var resolver: NodeResolver

    @BeforeTest
    fun setUp() {
        resolver = NodeResolver()
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
        // There's an A record, but no Infinity node, hence the null
        assertNull(resolver.resolve("example.com"))
    }

    @Test
    fun `returns null`() = runBlocking {
        assertNull(resolver.resolve("b.c"))
    }
}
