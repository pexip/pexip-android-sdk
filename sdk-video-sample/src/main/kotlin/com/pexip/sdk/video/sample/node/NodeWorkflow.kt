package com.pexip.sdk.video.sample.node

import android.os.Parcelable
import com.pexip.sdk.video.NodeResolver
import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
import com.squareup.workflow1.action
import com.squareup.workflow1.ui.toParcelable
import com.squareup.workflow1.ui.toSnapshot
import kotlinx.parcelize.Parcelize

class NodeWorkflow internal constructor(private val resolver: NodeResolver) :
    StatefulWorkflow<NodeProps, NodeState, NodeOutput, NodeRendering>() {

    constructor() : this(NodeResolver())

    override fun initialState(props: NodeProps, snapshot: Snapshot?): NodeState =
        snapshot?.toParcelable() ?: NodeState.ResolvingNode

    override fun snapshotState(state: NodeState): Snapshot = state.toSnapshot()

    override fun render(
        renderProps: NodeProps,
        renderState: NodeState,
        context: RenderContext,
    ): NodeRendering {
        context.resolveSideEffect(renderProps)
        return when (renderState) {
            is NodeState.ResolvingNode -> NodeRendering.ResolvingNode
            is NodeState.Failure -> NodeRendering.Failure(
                t = renderState.t,
                onBackClick = context.eventHandler({ "OnBackClick" }) { setOutput(NodeOutput.Back) }
            )
        }
    }

    private fun RenderContext.resolveSideEffect(props: NodeProps) =
        runningSideEffect(props.toString()) {
            val action = try {
                onNode(resolver.resolve(props.host))
            } catch (t: Throwable) {
                onError(t)
            }
            actionSink.send(action)
        }

    private fun onNode(address: String?) = action({ "OnNode($address)" }) {
        if (address != null) {
            setOutput(NodeOutput.Node(address))
        } else {
            val e = NoSuchElementException("No node found.")
            state = NodeState.Failure(e)
        }
    }

    private fun onError(t: Throwable) = action({ "OnError($t)" }) { state = NodeState.Failure(t) }
}

@JvmInline
value class NodeProps(val host: String)

sealed class NodeState : Parcelable {

    @Parcelize
    object ResolvingNode : NodeState()

    @Parcelize
    data class Failure(val t: Throwable) : NodeState()
}

sealed class NodeOutput {

    data class Node(val address: String) : NodeOutput()

    object Back : NodeOutput() {

        override fun toString(): String = "Back"
    }
}

sealed class NodeRendering {

    object ResolvingNode : NodeRendering() {

        override fun toString(): String = "ResolvingNode"
    }

    data class Failure(val t: Throwable, val onBackClick: () -> Unit) : NodeRendering()
}
