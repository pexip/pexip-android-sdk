package com.pexip.sdk.sample

import com.pexip.sdk.sample.alias.AliasOutput
import com.pexip.sdk.sample.conference.ConferenceOutput
import com.pexip.sdk.sample.pinchallenge.PinChallengeOutput
import com.pexip.sdk.sample.welcome.WelcomeOutput
import com.squareup.workflow1.WorkflowAction

typealias SampleAction = WorkflowAction<Unit, SampleState, SampleOutput>

data class OnWelcomeOutput(val output: WelcomeOutput) : SampleAction() {

    override fun Updater.apply() {
        when (output) {
            is WelcomeOutput.Next -> state = SampleState.Alias
            is WelcomeOutput.Back -> setOutput(SampleOutput.Finish)
        }
    }
}

data class OnAliasOutput(val output: AliasOutput) : SampleAction() {

    override fun Updater.apply() {
        when (output) {
            is AliasOutput.Conference -> state = SampleState.Conference(
                node = output.node,
                conferenceAlias = output.conferenceAlias,
                presentationInMain = output.presentationInMain,
                response = output.response
            )
            is AliasOutput.PinChallenge -> state = SampleState.PinChallenge(
                node = output.node,
                conferenceAlias = output.conferenceAlias,
                presentationInMain = output.presentationInMain,
                required = output.required
            )
            is AliasOutput.Toast -> setOutput(SampleOutput.Toast(output.message))
            is AliasOutput.Back -> state = SampleState.Welcome
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
                presentationInMain = s.presentationInMain,
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
