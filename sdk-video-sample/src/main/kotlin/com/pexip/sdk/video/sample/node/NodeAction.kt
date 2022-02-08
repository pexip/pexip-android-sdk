package com.pexip.sdk.video.sample.node

import com.squareup.workflow1.WorkflowAction
import okhttp3.HttpUrl

typealias NodeAction = WorkflowAction<NodeProps, NodeState, NodeOutput>

class OnBackClick : NodeAction() {

    override fun Updater.apply() {
        setOutput(NodeOutput.Back)
    }
}

data class OnNode(val address: HttpUrl?) : NodeAction() {

    override fun Updater.apply() {
        if (address != null) {
            setOutput(NodeOutput.Node(address))
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
