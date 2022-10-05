package com.pexip.sdk.sample.preflight

import com.pexip.sdk.sample.alias.AliasOutput
import com.pexip.sdk.sample.displayname.DisplayNameOutput
import com.pexip.sdk.sample.pinchallenge.PinChallengeOutput
import com.squareup.workflow1.WorkflowAction

typealias PreflightAction = WorkflowAction<PreflightProps, PreflightState, PreflightOutput>

class OnCallClick : PreflightAction() {

    override fun Updater.apply() {
        state = PreflightState(PreflightDestination.DisplayName)
    }
}

class OnBackClick : PreflightAction() {

    override fun Updater.apply() {
        setOutput(PreflightOutput.Back)
    }
}

class OnDisplayNameOutput(private val output: DisplayNameOutput) : PreflightAction() {

    override fun Updater.apply() {
        val destination = when (output) {
            is DisplayNameOutput.Next -> PreflightDestination.Alias
            is DisplayNameOutput.Back -> null
        }
        state = PreflightState(destination)
    }
}

class OnAliasOutput(private val output: AliasOutput) : PreflightAction() {

    override fun Updater.apply() {
        when (output) {
            is AliasOutput.Conference -> {
                val output = PreflightOutput.Conference(
                    node = output.node,
                    conferenceAlias = output.conferenceAlias,
                    presentationInMain = output.presentationInMain,
                    response = output.response
                )
                setOutput(output)
            }
            is AliasOutput.PinChallenge -> state = PreflightState(
                destination = PreflightDestination.PinChallenge(
                    node = output.node,
                    conferenceAlias = output.conferenceAlias,
                    presentationInMain = output.presentationInMain,
                    required = output.required
                )
            )
            is AliasOutput.Toast -> setOutput(PreflightOutput.Toast(output.message))
            is AliasOutput.Back -> state = PreflightState(null)
        }
    }
}

class OnPinChallengeOutput(private val output: PinChallengeOutput) : PreflightAction() {

    override fun Updater.apply() {
        val s = checkNotNull(state.destination as? PreflightDestination.PinChallenge)
        when (output) {
            is PinChallengeOutput.Response -> {
                val output = PreflightOutput.Conference(
                    node = s.node,
                    conferenceAlias = s.conferenceAlias,
                    presentationInMain = s.presentationInMain,
                    response = output.response
                )
                setOutput(output)
            }
            is PinChallengeOutput.Back -> state = PreflightState(null)
        }
    }
}
