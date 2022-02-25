package com.pexip.sdk.video

import com.pexip.sdk.video.Node.Companion.toNode
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@RunWith(RobolectricTestRunner::class)
internal class NodeResolverTest {

    private lateinit var builder: JoinDetails.Builder
    private lateinit var resolver: NodeResolver

    @BeforeTest
    fun setUp() {
        builder = JoinDetails.Builder()
            .alias("test")
            .displayName("John")
        resolver = NodeResolver.Builder()
            .dnssec(false)
            .client(OkHttpClient())
            .build()
    }

    @Test
    fun `returns DNS SRV record`(): Unit = runBlocking {
        assertEquals(
            expected = "https://pexipdemo.com".toNode(),
            actual = resolver.resolve(builder.host("pexip.com").build())
        )
    }

    @Test
    fun `returns A record`(): Unit = runBlocking {
        // There's an A record, but no Infinity node, hence the null
        assertNull(resolver.resolve(builder.host("example.com").build()))
    }

    @Test
    fun `returns null`(): Unit = runBlocking {
        assertNull(resolver.resolve(builder.host("b.c").build()))
    }
}
