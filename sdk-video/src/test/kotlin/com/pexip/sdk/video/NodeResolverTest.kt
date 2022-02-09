package com.pexip.sdk.video

import kotlinx.coroutines.runBlocking
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@RunWith(RobolectricTestRunner::class)
internal class NodeResolverTest {

    private lateinit var resolver: NodeResolver

    @BeforeTest
    fun setUp() {
        resolver = NodeResolver.Builder()
            .dnssec(false)
            .client(OkHttpClient())
            .build()
    }

    @Test
    fun `returns DNS SRV record`(): Unit = runBlocking {
        assertEquals(
            expected = HttpUrl.Builder()
                .scheme("https")
                .host("pexipdemo.com")
                .build(),
            actual = resolver.resolve("pexip.com")
        )
    }

    @Test
    fun `returns A record`(): Unit = runBlocking {
        // There's an A record, but no Infinity node, hence the null
        assertNull(resolver.resolve("example.com"))
    }

    @Test
    fun `returns null`(): Unit = runBlocking {
        assertNull(resolver.resolve("b.c"))
    }
}
