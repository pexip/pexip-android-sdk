package com.pexip.sdk.sample

import com.pexip.sdk.sample.alias.AliasWorkflow
import com.pexip.sdk.sample.conference.ConferenceProps
import com.pexip.sdk.sample.conference.ConferenceWorkflow
import com.pexip.sdk.sample.node.NodeProps
import com.pexip.sdk.sample.node.NodeWorkflow
import com.pexip.sdk.sample.pinchallenge.PinChallengeProps
import com.pexip.sdk.sample.pinchallenge.PinChallengeWorkflow
import com.pexip.sdk.sample.pinrequirement.PinRequirementProps
import com.pexip.sdk.sample.pinrequirement.PinRequirementWorkflow
import com.pexip.sdk.sample.welcome.WelcomeWorkflow
import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
import com.squareup.workflow1.renderChild
import com.squareup.workflow1.ui.toParcelable
import com.squareup.workflow1.ui.toSnapshot
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SampleWorkflow @Inject constructor(
    private val welcomeWorkflow: WelcomeWorkflow,
    private val aliasWorkflow: AliasWorkflow,
    private val nodeWorkflow: NodeWorkflow,
    private val pinRequirementWorkflow: PinRequirementWorkflow,
    private val pinChallengeWorkflow: PinChallengeWorkflow,
    private val conferenceWorkflow: ConferenceWorkflow,
) : StatefulWorkflow<Unit, SampleState, SampleOutput, Any>() {

    override fun initialState(props: Unit, snapshot: Snapshot?): SampleState =
        snapshot?.toParcelable() ?: SampleState.Welcome

    override fun snapshotState(state: SampleState): Snapshot = state.toSnapshot()

    override fun render(
        renderProps: Unit,
        renderState: SampleState,
        context: RenderContext,
    ): Any = when (renderState) {
        is SampleState.Welcome -> context.renderChild(
            child = welcomeWorkflow,
            handler = ::OnWelcomeOutput
        )
        is SampleState.Alias -> context.renderChild(
            child = aliasWorkflow,
            handler = ::OnAliasOutput
        )
        is SampleState.Node -> context.renderChild(
            child = nodeWorkflow,
            props = NodeProps(renderState.host),
            handler = ::OnNodeOutput
        )
        is SampleState.PinRequirement -> context.renderChild(
            child = pinRequirementWorkflow,
            props = PinRequirementProps(
                conferenceAlias = renderState.conferenceAlias,
                node = renderState.node
            ),
            handler = ::OnPinRequirementOutput
        )
        is SampleState.PinChallenge -> context.renderChild(
            child = pinChallengeWorkflow,
            props = PinChallengeProps(
                node = renderState.node,
                conferenceAlias = renderState.conferenceAlias,
                required = renderState.required
            ),
            handler = ::OnPinChallengeOutput
        )
        is SampleState.Conference -> context.renderChild(
            child = conferenceWorkflow,
            props = ConferenceProps(
                node = renderState.node,
                conferenceAlias = renderState.conferenceAlias,
                presentationInMain = renderState.presentationInMain,
                response = renderState.response
            ),
            handler = ::OnConferenceOutput
        )
    }
}
