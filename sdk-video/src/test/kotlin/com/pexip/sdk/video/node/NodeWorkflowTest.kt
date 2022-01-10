package com.pexip.sdk.video.node

import com.pexip.sdk.workflow.core.ExperimentalWorkflowApi
import com.pexip.sdk.workflow.test.test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.random.Random
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

@OptIn(ExperimentalWorkflowApi::class)
@RunWith(RobolectricTestRunner::class)
class NodeWorkflowTest {

    private lateinit var uri: String

    @BeforeTest
    fun setUp() {
        uri = "${Random.nextInt()}@${Random.nextInt()}"
    }

    @Test
    fun `outputs Node`() {
        val address = "192.168.0.1"
        val workflow = NodeWorkflow { address }
        workflow.test(NodeProps(uri)) {
            assertEquals(NodeRendering.ResolvingNode, awaitRendering())
            assertEquals(NodeOutput.Node(address), awaitOutput())
        }
    }

    @Test
    fun `outputs Back`() {
        val t = Throwable()
        val workflow = NodeWorkflow { throw t }
        workflow.test(NodeProps(uri)) {
            assertEquals(NodeRendering.ResolvingNode, awaitRendering())
            val rendering = assertIs<NodeRendering.Failure>(awaitRendering())
            assertEquals(t, rendering.t)
            rendering.onBackClick()
            assertEquals(NodeOutput.Back, awaitOutput())
        }
    }
}
