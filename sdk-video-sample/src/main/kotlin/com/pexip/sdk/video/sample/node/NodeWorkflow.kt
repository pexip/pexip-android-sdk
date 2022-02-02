package com.pexip.sdk.video.sample.node

import com.pexip.sdk.video.NodeResolver
import com.pexip.sdk.video.sample.send
import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
import com.squareup.workflow1.ui.toParcelable
import com.squareup.workflow1.ui.toSnapshot

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
                onBackClick = context.send(::OnBackClick)
            )
        }
    }

    private fun RenderContext.resolveSideEffect(props: NodeProps) =
        runningSideEffect(props.toString()) {
            val action = try {
                OnNode(resolver.resolve(props.host))
            } catch (t: Throwable) {
                OnError(t)
            }
            actionSink.send(action)
        }
}
