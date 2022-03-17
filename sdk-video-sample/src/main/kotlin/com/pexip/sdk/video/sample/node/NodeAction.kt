package com.pexip.sdk.video.sample.node

import com.pexip.sdk.video.node.Node
import com.squareup.workflow1.WorkflowAction

typealias NodeAction = WorkflowAction<NodeProps, NodeState, NodeOutput>

class OnBackClick : NodeAction() {

    override fun Updater.apply() {
        setOutput(NodeOutput.Back)
    }
}

data class OnNode(val node: Node?) : NodeAction() {

    override fun Updater.apply() {
        if (node != null) {
            setOutput(NodeOutput.Node(node))
        } else {
            val e = NoSuchElementException("No node found.")
            state = NodeState.Failure(e)
        }
    }
}

data class OnError(val t: Throwable) : NodeAction() {

    override fun Updater.apply() {
        state = NodeState.Failure(t)
    }
}
