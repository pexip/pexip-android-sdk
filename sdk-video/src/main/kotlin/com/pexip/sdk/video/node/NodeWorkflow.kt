package com.pexip.sdk.video.node

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import com.pexip.sdk.video.node.internal.MiniDnsNodeResolver
import com.pexip.sdk.video.node.internal.NodeResolver
import com.pexip.sdk.workflow.core.ExperimentalWorkflowApi
import com.pexip.sdk.workflow.core.Workflow

@ExperimentalWorkflowApi
class NodeWorkflow internal constructor(private val resolver: NodeResolver) :
    Workflow<NodeProps, NodeOutput, NodeRendering> {

    constructor() : this(MiniDnsNodeResolver())

    private sealed class State {
        object ResolvingNode : State()
        data class Failure(val t: Throwable) : State()
    }

    @SuppressLint("ComposableNaming")
    @Composable
    override fun render(props: NodeProps, onOutput: (NodeOutput) -> Unit): NodeRendering {
        val (state, onStateChange) = remember { mutableStateOf<State>(State.ResolvingNode) }
        val currentOnOutput by rememberUpdatedState(onOutput)
        LaunchedEffect(props) {
            try {
                val address = resolver.resolve(props.uri)
                currentOnOutput(NodeOutput.Node(address))
            } catch (t: Throwable) {
                onStateChange(State.Failure(t))
            }
        }
        return when (state) {
            is State.ResolvingNode -> NodeRendering.ResolvingNode
            is State.Failure -> NodeRendering.Failure(
                t = state.t,
                onBackClick = { onOutput(NodeOutput.Back) }
            )
        }
    }
}

@Immutable
@JvmInline
value class NodeProps(val uri: String)

@Immutable
sealed class NodeOutput {

    @Immutable
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

    @Immutable
    object Back : NodeOutput() {

        override fun toString(): String = "Back"
    }
}

@Immutable
sealed class NodeRendering {

    @Immutable
    object ResolvingNode : NodeRendering() {

        override fun toString(): String = "ResolvingNode"
    }

    @Immutable
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
