package com.pexip.sdk.video.node

import com.squareup.workflow1.testing.launchForTestingFromStartWith
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlin.random.Random
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

@OptIn(ExperimentalCoroutinesApi::class)
class NodeWorkflowTest {

    private lateinit var dispatcher: TestCoroutineDispatcher
    private lateinit var uri: String

    @BeforeTest
    fun setUp() {
        dispatcher = TestCoroutineDispatcher()
        uri = "${Random.nextInt()}@${Random.nextInt()}"
    }

    @AfterTest
    fun tearDown() {
        dispatcher.cleanupTestCoroutines()
    }

    @Test
    fun `outputs Node`() {
        val address = "192.168.0.1"
        val workflow = NodeWorkflow { address }
        workflow.launchForTestingFromStartWith(NodeProps(uri), context = dispatcher) {
            assertEquals(NodeRendering.ResolvingNode, awaitNextRendering())
            assertEquals(NodeOutput.Node(address), awaitNextOutput())
        }
    }

    @Test
    fun `outputs Back`() {
        val t = Throwable()
        val workflow = NodeWorkflow {
            delay(100)
            throw t
        }
        workflow.launchForTestingFromStartWith(NodeProps(uri), context = dispatcher) {
            assertEquals(NodeRendering.ResolvingNode, awaitNextRendering())
            dispatcher.advanceTimeBy(100)
            val rendering = assertIs<NodeRendering.Failure>(awaitNextRendering())
            assertEquals(t, rendering.t)
            rendering.onBackClick()
            assertEquals(NodeOutput.Back, awaitNextOutput())
        }
    }
}
