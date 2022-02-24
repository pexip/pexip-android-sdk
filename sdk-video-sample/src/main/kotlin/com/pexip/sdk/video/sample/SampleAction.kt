package com.pexip.sdk.video.sample

import com.pexip.sdk.video.JoinDetails
import com.pexip.sdk.video.sample.alias.AliasOutput
import com.pexip.sdk.video.sample.conference.ConferenceOutput
import com.pexip.sdk.video.sample.node.NodeOutput
import com.pexip.sdk.video.sample.pinchallenge.PinChallengeOutput
import com.pexip.sdk.video.sample.pinrequirement.PinRequirementOutput
import com.squareup.workflow1.WorkflowAction

typealias SampleAction = WorkflowAction<SampleProps, SampleState, SampleOutput>

data class OnAliasOutput(val output: AliasOutput) : SampleAction() {

    override fun Updater.apply() {
        when (output) {
            is AliasOutput.Alias -> {
                val joinDetails = JoinDetails.Builder()
                    .alias(output.alias)
                    .host(output.alias.split("@").last())
                    .displayName(props.displayName)
                    .build()
                state = SampleState.Node(joinDetails)
            }
            is AliasOutput.Back -> setOutput(SampleOutput.Finish)
        }
    }
}

data class OnNodeOutput(val output: NodeOutput) : SampleAction() {

    override fun Updater.apply() {
        val s = checkNotNull(state as? SampleState.Node) { "Invalid state: $state" }
        state = when (output) {
            is NodeOutput.Node -> SampleState.PinRequirement(s.joinDetails, output.node)
            is NodeOutput.Back -> SampleState.Alias
        }
    }
}

data class OnPinRequirementOutput(val output: PinRequirementOutput) : SampleAction() {

    override fun Updater.apply() {
        val s = checkNotNull(state as? SampleState.PinRequirement) { "Invalid state: $state" }
        state = when (output) {
            is PinRequirementOutput.Some -> SampleState.PinChallenge(
                joinDetails = s.joinDetails,
                node = s.node,
                required = output.required
            )
            is PinRequirementOutput.None -> SampleState.Conference(output.token)
            is PinRequirementOutput.Back -> SampleState.Alias
        }
    }
}

data class OnPinChallengeOutput(val output: PinChallengeOutput) : SampleAction() {

    override fun Updater.apply() {
        state = when (output) {
            is PinChallengeOutput.Token -> SampleState.Conference(output.token)
            is PinChallengeOutput.Back -> SampleState.Alias
        }
    }
}

data class OnConferenceOutput(val output: ConferenceOutput) : SampleAction() {

    override fun Updater.apply() {
        state = when (output) {
            is ConferenceOutput.Back -> SampleState.Alias
        }
    }
}
