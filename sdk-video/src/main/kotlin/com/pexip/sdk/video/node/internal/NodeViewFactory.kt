package com.pexip.sdk.video.node.internal

import com.pexip.sdk.video.node.NodeRendering.Failure
import com.pexip.sdk.video.node.NodeRendering.ResolvingNode
import com.squareup.workflow1.ui.compose.composeViewFactory

internal object NodeViewFactory {

    val ResolvingNodeViewFactory = composeViewFactory<ResolvingNode> { _, _ ->
        ResolvingNodeScreen()
    }

    val FailureViewFactory = composeViewFactory<Failure> { rendering, _ ->
        FailureScreen(rendering = rendering)
    }
}
