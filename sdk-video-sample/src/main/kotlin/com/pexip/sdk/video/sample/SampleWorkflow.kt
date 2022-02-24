package com.pexip.sdk.video.sample

import com.pexip.sdk.video.sample.alias.AliasWorkflow
import com.pexip.sdk.video.sample.conference.ConferenceProps
import com.pexip.sdk.video.sample.conference.ConferenceWorkflow
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

class SampleWorkflow(
    private val aliasWorkflow: AliasWorkflow,
    private val nodeWorkflow: NodeWorkflow,
    private val pinRequirementWorkflow: PinRequirementWorkflow,
    private val pinChallengeWorkflow: PinChallengeWorkflow,
    private val conferenceWorkflow: ConferenceWorkflow,
) : StatefulWorkflow<SampleProps, SampleState, SampleOutput, Any>() {

    override fun initialState(props: SampleProps, snapshot: Snapshot?): SampleState =
        snapshot?.toParcelable() ?: SampleState.Alias

    override fun snapshotState(state: SampleState): Snapshot = state.toSnapshot()

    override fun render(
        renderProps: SampleProps,
        renderState: SampleState,
        context: RenderContext,
    ): Any = when (renderState) {
        is SampleState.Alias -> context.renderChild(
            child = aliasWorkflow,
            handler = ::OnAliasOutput
        )
        is SampleState.Node -> context.renderChild(
            child = nodeWorkflow,
            props = NodeProps(renderState.joinDetails),
            handler = ::OnNodeOutput
        )
        is SampleState.PinRequirement -> context.renderChild(
            child = pinRequirementWorkflow,
            props = PinRequirementProps(renderState.node, renderState.joinDetails),
            handler = ::OnPinRequirementOutput
        )
        is SampleState.PinChallenge -> context.renderChild(
            child = pinChallengeWorkflow,
            props = PinChallengeProps(
                node = renderState.node,
                joinDetails = renderState.joinDetails,
                required = renderState.required
            ),
            handler = ::OnPinChallengeOutput
        )
        is SampleState.Conference -> context.renderChild(
            child = conferenceWorkflow,
            props = ConferenceProps(renderState.token),
            handler = ::OnConferenceOutput
        )
    }
}
