package com.pexip.sdk.video.sample

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
                state = SampleState.Node(
                    conferenceAlias = output.conferenceAlias,
                    host = output.host
                )
            }
            is AliasOutput.Back -> setOutput(SampleOutput.Finish)
        }
    }
}

data class OnNodeOutput(val output: NodeOutput) : SampleAction() {

    override fun Updater.apply() {
        val s = checkNotNull(state as? SampleState.Node) { "Invalid state: $state" }
        state = when (output) {
            is NodeOutput.Node -> SampleState.PinRequirement(output.node, s.conferenceAlias)
            is NodeOutput.Back -> SampleState.Alias
        }
    }
}

data class OnPinRequirementOutput(val output: PinRequirementOutput) : SampleAction() {

    override fun Updater.apply() {
        val s = checkNotNull(state as? SampleState.PinRequirement) { "Invalid state: $state" }
        state = when (output) {
            is PinRequirementOutput.Some -> SampleState.PinChallenge(
                node = s.node,
                conferenceAlias = s.conferenceAlias,
                required = output.required
            )
            is PinRequirementOutput.None -> SampleState.Conference(
                node = s.node,
                conferenceAlias = s.conferenceAlias,
                response = output.response
            )
            is PinRequirementOutput.Back -> SampleState.Alias
        }
    }
}

data class OnPinChallengeOutput(val output: PinChallengeOutput) : SampleAction() {

    override fun Updater.apply() {
        val s = checkNotNull(state as? SampleState.PinChallenge) { "Invalid state: $state" }
        state = when (output) {
            is PinChallengeOutput.Response -> SampleState.Conference(
                node = s.node,
                conferenceAlias = s.conferenceAlias,
                response = output.response
            )
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
