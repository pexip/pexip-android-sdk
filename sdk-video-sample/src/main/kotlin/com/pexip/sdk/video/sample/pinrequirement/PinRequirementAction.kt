package com.pexip.sdk.video.sample.pinrequirement

import com.pexip.sdk.api.infinity.RequestTokenResponse
import com.pexip.sdk.api.infinity.RequiredPinException
import com.squareup.workflow1.WorkflowAction

typealias PinRequirementAction = WorkflowAction<PinRequirementProps, PinRequirementState, PinRequirementOutput>

class OnBackClick : PinRequirementAction() {

    override fun Updater.apply() {
        setOutput(PinRequirementOutput.Back)
    }
}

data class OnResponse(val response: RequestTokenResponse) : PinRequirementAction() {

    override fun Updater.apply() {
        setOutput(PinRequirementOutput.None(response))
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
