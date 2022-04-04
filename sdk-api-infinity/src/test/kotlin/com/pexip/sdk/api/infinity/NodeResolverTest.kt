package com.pexip.sdk.api.infinity

import androidx.test.core.app.ApplicationProvider
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import java.net.URL
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(ParameterizedRobolectricTestRunner::class)
internal class NodeResolverTest(private val host: String, private val url: String?) {

    private lateinit var resolver: NodeResolver

    @BeforeTest
    fun setUp() {
        NodeResolver.initialize(ApplicationProvider.getApplicationContext())
        resolver = NodeResolver.create()
    }

    @Test
    fun `onSuccess is called`() {
        assertEquals(
            expected = listOfNotNull(url?.let(::URL)),
            actual = resolver.resolve(host).execute()
        )
    }

    companion object {

        @JvmStatic
        @get:ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
        val testCases = listOf(
            // SRV record
            arrayOf("pexip.com", "https://pexipdemo.com"),
            // A record
            arrayOf("google.com", "https://google.com"),
            // Not a real domain
            arrayOf("b.c", null)
        )
    }
}
