package com.pexip.sdk.video.node

import android.os.Parcelable
import com.pexip.sdk.video.node.internal.MiniDnsNodeResolver
import com.pexip.sdk.video.node.internal.NodeResolver
import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
import com.squareup.workflow1.action
import com.squareup.workflow1.ui.toParcelable
import com.squareup.workflow1.ui.toSnapshot
import kotlinx.parcelize.Parcelize

class NodeWorkflow internal constructor(private val resolver: NodeResolver) :
    StatefulWorkflow<NodeProps, NodeState, NodeOutput, NodeRendering>() {

    constructor() : this(MiniDnsNodeResolver())

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
                onNode(resolver.resolve(props.uri))
            } catch (t: Throwable) {
                onError(t)
            }
            actionSink.send(action)
        }

    private fun onNode(address: String) = action({ "OnNode($address)" }) {
        setOutput(NodeOutput.Node(address))
    }

    private fun onError(t: Throwable) = action({ "OnError($t)" }) { state = NodeState.Failure(t) }
}

@JvmInline
value class NodeProps(val uri: String)

sealed class NodeState : Parcelable {

    @Parcelize
    object ResolvingNode : NodeState()

    @Parcelize
    data class Failure(val t: Throwable) : NodeState()
}

sealed class NodeOutput {

    class Node(val address: String) : NodeOutput() {

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Node) return false
            if (address != other.address) return false
            return true
        }

        override fun hashCode(): Int = address.hashCode()

        override fun toString(): String = "Node(address=$address)"
    }

    object Back : NodeOutput() {

        override fun toString(): String = "Back"
    }
}

sealed class NodeRendering {

    object ResolvingNode : NodeRendering() {

        override fun toString(): String = "ResolvingNode"
    }

    class Failure(val t: Throwable, val onBackClick: () -> Unit) : NodeRendering() {

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Failure) return false
            if (t != other.t) return false
            if (onBackClick != other.onBackClick) return false
            return true
        }

        override fun hashCode(): Int {
            var result = t.hashCode()
            result = 31 * result + onBackClick.hashCode()
            return result
        }

        override fun toString(): String = "Failure(t=$t, onBackClick=$onBackClick)"
    }
}
