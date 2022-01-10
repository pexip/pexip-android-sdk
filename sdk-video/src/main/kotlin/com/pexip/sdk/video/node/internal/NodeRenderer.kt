package com.pexip.sdk.video.node.internal

import com.pexip.sdk.video.node.NodeRendering.Failure
import com.pexip.sdk.video.node.NodeRendering.ResolvingNode
import com.pexip.sdk.workflow.ui.ExperimentalWorkflowUiApi
import com.pexip.sdk.workflow.ui.renderer

@ExperimentalWorkflowUiApi
internal object NodeRenderer {

    val ResolvingNodeRenderer = renderer<ResolvingNode> {
        ResolvingNodeScreen(modifier = it)
    }

    val FailureRenderer = renderer<Failure> {
        FailureScreen(rendering = this, modifier = it)
    }
}
