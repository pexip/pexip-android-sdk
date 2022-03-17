package com.pexip.sdk.video.sample.pinrequirement

import com.pexip.sdk.video.token.RequiredPinException
import com.pexip.sdk.video.token.Token
import com.squareup.workflow1.WorkflowAction

typealias PinRequirementAction = WorkflowAction<PinRequirementProps, PinRequirementState, PinRequirementOutput>

class OnBackClick : PinRequirementAction() {

    override fun Updater.apply() {
        setOutput(PinRequirementOutput.Back)
    }
}

data class OnToken(val token: Token) : PinRequirementAction() {

    override fun Updater.apply() {
        setOutput(PinRequirementOutput.None(token))
    }
}

data class OnError(val t: Throwable) : PinRequirementAction() {

    override fun Updater.apply() {
        when (t) {
            is RequiredPinException -> setOutput(PinRequirementOutput.Some(t.guestPin))
            else -> state = PinRequirementState.Failure(t)
        }
    }
}
