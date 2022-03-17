package com.pexip.sdk.video.node

import com.pexip.sdk.video.internal.OkHttpClient
import com.pexip.sdk.video.node.Node.Companion.toNode
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(ParameterizedRobolectricTestRunner::class)
internal class NodeResolverTest(private val host: String, private val expectedNode: Node?) {

    private lateinit var resolver: NodeResolver

    @BeforeTest
    fun setUp() {
        resolver = NodeResolver.Builder()
            .dnssec(false)
            .client(OkHttpClient)
            .build()
    }

    @Test
    fun `onSuccess is called`() {
        val callback = object : NodeResolver.Callback {

            @Volatile
            var node: Node? = null

            override fun onSuccess(resolver: NodeResolver, node: Node?) {
                this.node = node
            }

            override fun onFailure(resolver: NodeResolver, t: Throwable) {
                throw t
            }
        }
        resolver.resolve(host, callback).get()
        assertEquals(expectedNode, callback.node)
    }

    companion object {

        @JvmStatic
        @get:ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
        val testCases = listOf(
            // SRV record
            arrayOf("pexip.com", "https://pexipdemo.com".toNode()),
            // A record, but no Infinity node
            arrayOf("example.com", null),
            // Not a real domain
            arrayOf("b.c", null)
        )
    }
}
