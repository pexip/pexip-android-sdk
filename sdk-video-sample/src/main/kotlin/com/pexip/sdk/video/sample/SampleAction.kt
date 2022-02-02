package com.pexip.sdk.video.sample

import com.pexip.sdk.video.sample.alias.AliasOutput
import com.pexip.sdk.video.sample.node.NodeOutput
import com.pexip.sdk.video.sample.pinchallenge.PinChallengeOutput
import com.pexip.sdk.video.sample.pinrequirement.PinRequirementOutput
import com.squareup.workflow1.WorkflowAction

typealias SampleAction = WorkflowAction<SampleProps, SampleState, SampleOutput>

data class OnAliasOutput(val output: AliasOutput) : SampleAction() {

    override fun Updater.apply() {
        when (output) {
            is AliasOutput.Alias -> state = SampleState.Node(
                alias = output.alias,
                host = output.alias.split("@").last()
            )
            is AliasOutput.Back -> setOutput(SampleOutput.Finish)
        }
    }
}

data class OnNodeOutput(val output: NodeOutput) : SampleAction() {

    override fun Updater.apply() {
        val s = checkNotNull(state as? SampleState.Node) { "Invalid state: $state" }
        state = when (output) {
            is NodeOutput.Node -> SampleState.PinRequirement(s.alias, output.address)
            is NodeOutput.Back -> SampleState.Alias
        }
    }
}

data class OnPinRequirementOutput(val output: PinRequirementOutput) : SampleAction() {

    override fun Updater.apply() {
        val s = checkNotNull(state as? SampleState.PinRequirement) { "Invalid state: $state" }
        state = when (output) {
            is PinRequirementOutput.Some -> SampleState.PinChallenge(
                alias = s.alias,
                nodeAddress = s.nodeAddress,
                required = output.required
            )
            is PinRequirementOutput.None -> SampleState.Alias
            is PinRequirementOutput.Back -> SampleState.Alias
        }
        val output = when (output) {
            is PinRequirementOutput.None -> SampleOutput.Toast(output.token.toString())
            else -> null
        }
        output?.let(::setOutput)
    }
}

data class OnPinChallengeOutput(val output: PinChallengeOutput) : SampleAction() {

    override fun Updater.apply() {
        state = when (output) {
            is PinChallengeOutput.Token -> SampleState.Alias
            is PinChallengeOutput.Back -> SampleState.Alias
        }
        val output = when (output) {
            is PinChallengeOutput.Token -> SampleOutput.Toast(output.token.toString())
            else -> null
        }
        output?.let(::setOutput)
    }
}
