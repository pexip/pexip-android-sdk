package com.pexip.sdk.video.sample

import com.pexip.sdk.video.sample.alias.AliasWorkflow
import com.pexip.sdk.video.sample.node.NodeProps
import com.pexip.sdk.video.sample.node.NodeWorkflow
import com.pexip.sdk.video.sample.pinchallenge.PinChallengeProps
import com.pexip.sdk.video.sample.pinchallenge.PinChallengeWorkflow
import com.pexip.sdk.video.sample.pinrequirement.PinRequirementProps
import com.pexip.sdk.video.sample.pinrequirement.PinRequirementWorkflow
import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
import com.squareup.workflow1.renderChild
import com.squareup.workflow1.ui.toParcelable
import com.squareup.workflow1.ui.toSnapshot

object SampleWorkflow : StatefulWorkflow<SampleProps, SampleState, SampleOutput, Any>() {

    private val AliasWorkflow = AliasWorkflow()
    private val NodeWorkflow = NodeWorkflow()
    private val PinRequirementWorkflow = PinRequirementWorkflow()
    private val PinChallengeWorkflow = PinChallengeWorkflow()

    override fun initialState(props: SampleProps, snapshot: Snapshot?): SampleState =
        snapshot?.toParcelable() ?: SampleState.Alias

    override fun snapshotState(state: SampleState): Snapshot = state.toSnapshot()

    override fun render(
        renderProps: SampleProps,
        renderState: SampleState,
        context: RenderContext,
    ): Any = when (renderState) {
        is SampleState.Alias -> context.renderChild(
            child = AliasWorkflow,
            handler = ::OnAliasOutput
        )
        is SampleState.Node -> context.renderChild(
            child = NodeWorkflow,
            props = NodeProps(renderState.host),
            handler = ::OnNodeOutput
        )
        is SampleState.PinRequirement -> context.renderChild(
            child = PinRequirementWorkflow,
            props = PinRequirementProps(
                nodeAddress = renderState.nodeAddress,
                alias = renderState.alias,
                displayName = renderProps.displayName
            ),
            handler = ::OnPinRequirementOutput
        )
        is SampleState.PinChallenge -> context.renderChild(
            child = PinChallengeWorkflow,
            props = PinChallengeProps(
                nodeAddress = renderState.nodeAddress,
                alias = renderState.alias,
                displayName = renderProps.displayName,
                required = renderState.required
            ),
            handler = ::OnPinChallengeOutput
        )
    }
}
