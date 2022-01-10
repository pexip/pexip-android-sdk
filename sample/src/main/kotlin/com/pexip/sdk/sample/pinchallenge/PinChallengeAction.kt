package com.pexip.sdk.sample.pinchallenge

import com.pexip.sdk.api.infinity.InvalidPinException
import com.pexip.sdk.api.infinity.RequestTokenResponse
import com.squareup.workflow1.WorkflowAction

typealias PinChallengeAction = WorkflowAction<PinChallengeProps, PinChallengeState, PinChallengeOutput>

class OnRequestToken : PinChallengeAction() {

    override fun Updater.apply() {
        state = state.copy(requesting = true)
    }
}

data class OnPinChange(val pin: String) : PinChallengeAction() {

    override fun Updater.apply() {
        state = state.copy(pin = pin.trim(), t = null)
    }
}

class OnSubmitClick : PinChallengeAction() {

    override fun Updater.apply() {
        state = state.copy(pinToSubmit = state.pin)
    }
}

class OnBackClick : PinChallengeAction() {

    override fun Updater.apply() {
        setOutput(PinChallengeOutput.Back)
    }
}

data class OnResponse(val response: RequestTokenResponse) : PinChallengeAction() {

    override fun Updater.apply() {
        setOutput(PinChallengeOutput.Response(response))
    }
}

data class OnInvalidPin(val e: InvalidPinException) : PinChallengeAction() {

    override fun Updater.apply() {
        state = state.copy(
            pin = "",
            t = e,
            requesting = false,
            pinToSubmit = null
        )
    }
}

data class OnError(val t: Throwable) : PinChallengeAction() {

    override fun Updater.apply() {
        state = state.copy(
            t = t,
            requesting = false,
            pinToSubmit = null
        )
    }
}
