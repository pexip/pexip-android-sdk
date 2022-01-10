package com.pexip.sdk.sample.node

import com.pexip.sdk.sample.node.NodeRendering.Failure
import com.pexip.sdk.sample.node.NodeRendering.ResolvingNode
import com.squareup.workflow1.ui.compose.composeViewFactory

object NodeViewFactory {

    val ResolvingNodeViewFactory = composeViewFactory<ResolvingNode> { _, _ ->
        ResolvingNodeScreen()
    }

    val FailureViewFactory = composeViewFactory<Failure> { rendering, _ ->
        FailureScreen(rendering = rendering)
    }
}
