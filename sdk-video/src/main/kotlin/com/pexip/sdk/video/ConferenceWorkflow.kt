package com.pexip.sdk.video

import android.os.Parcelable
import com.pexip.sdk.video.node.NodeOutput
import com.pexip.sdk.video.node.NodeProps
import com.pexip.sdk.video.node.NodeWorkflow
import com.pexip.sdk.video.pin.PinChallengeOutput
import com.pexip.sdk.video.pin.PinChallengeProps
import com.pexip.sdk.video.pin.PinChallengeWorkflow
import com.pexip.sdk.video.pin.PinRequirementOutput
import com.pexip.sdk.video.pin.PinRequirementProps
import com.pexip.sdk.video.pin.PinRequirementWorkflow
import com.squareup.workflow1.Snapshot
import com.squareup.workflow1.StatefulWorkflow
import com.squareup.workflow1.action
import com.squareup.workflow1.ui.toParcelable
import com.squareup.workflow1.ui.toSnapshot
import kotlinx.parcelize.Parcelize

class ConferenceWorkflow :
    StatefulWorkflow<ConferenceProps, ConferenceState, ConferenceOutput, Any>() {

    private val nodeWorkflow = NodeWorkflow()
    private val pinRequirementWorkflow = PinRequirementWorkflow()
    private val pinChallengeWorkflow = PinChallengeWorkflow()

    override fun initialState(props: ConferenceProps, snapshot: Snapshot?): ConferenceState =
        snapshot?.toParcelable() ?: ConferenceState.Node

    override fun snapshotState(state: ConferenceState): Snapshot = state.toSnapshot()

    override fun render(
        renderProps: ConferenceProps,
        renderState: ConferenceState,
        context: RenderContext,
    ): Any = when (renderState) {
        is ConferenceState.Node -> context.renderChild(
            child = nodeWorkflow,
            props = NodeProps(renderProps.uri),
            handler = ::onNodeOutput
        )
        is ConferenceState.PinRequirement -> context.renderChild(
            child = pinRequirementWorkflow,
            props = PinRequirementProps(
                nodeAddress = renderState.nodeAddress,
                conferenceAlias = renderProps.uri,
                displayName = renderProps.displayName
            ),
            handler = ::onPinRequirementOutput
        )
        is ConferenceState.PinChallenge -> context.renderChild(
            child = pinChallengeWorkflow,
            props = PinChallengeProps(
                nodeAddress = renderState.nodeAddress,
                conferenceAlias = renderProps.uri,
                displayName = renderProps.displayName,
                required = renderState.required
            ),
            handler = ::onPinChallengeOutput
        )
    }

    private fun onNodeOutput(output: NodeOutput) = action({ "OnNodeOutput($output)" }) {
        when (output) {
            is NodeOutput.Node -> state = ConferenceState.PinRequirement(output.address)
            is NodeOutput.Back -> setOutput(ConferenceOutput.Finish)
        }
    }

    private fun onPinRequirementOutput(output: PinRequirementOutput) =
        action({ "OnPinRequirementOutput($output)" }) {
            when (output) {
                is PinRequirementOutput.Some -> {
                    state = ConferenceState.PinChallenge(
                        nodeAddress = (state as ConferenceState.PinRequirement).nodeAddress,
                        required = output.required
                    )
                }
                is PinRequirementOutput.None -> setOutput(ConferenceOutput.Finish)
                is PinRequirementOutput.Back -> setOutput(ConferenceOutput.Finish)
            }
        }

    private fun onPinChallengeOutput(output: PinChallengeOutput) =
        action({ "OnPinChallengeOutput($output)" }) {
            setOutput(ConferenceOutput.Finish)
        }
}

sealed class ConferenceState : Parcelable {

    @Parcelize
    object Node : ConferenceState()

    @Parcelize
    data class PinRequirement(val nodeAddress: String) : ConferenceState()

    @Parcelize
    data class PinChallenge(val nodeAddress: String, val required: Boolean) : ConferenceState()
}

sealed class ConferenceOutput {

    object Finish : ConferenceOutput()
}
