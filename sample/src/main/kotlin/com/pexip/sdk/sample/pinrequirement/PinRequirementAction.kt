package com.pexip.sdk.sample.pinrequirement

import com.pexip.sdk.api.infinity.RequestTokenResponse
import com.squareup.workflow1.WorkflowAction
import java.net.URL

typealias PinRequirementAction = WorkflowAction<PinRequirementProps, PinRequirementState, PinRequirementOutput>

data class OnNode(val node: URL) : PinRequirementAction() {

    override fun Updater.apply() {
        state = PinRequirementState.ResolvingPinRequirement(node)
    }
}

data class OnResponse(
    val node: URL,
    val conferenceAlias: String,
    val response: RequestTokenResponse,
) : PinRequirementAction() {

    override fun Updater.apply() {
        setOutput(PinRequirementOutput.None(node, conferenceAlias, response))
    }
}

data class OnRequiredPin(
    val node: URL,
    val conferenceAlias: String,
    val required: Boolean,
) : PinRequirementAction() {

    override fun Updater.apply() {
        setOutput(PinRequirementOutput.Some(node, conferenceAlias, required))
    }
}

data class OnError(val t: Throwable) : PinRequirementAction() {

    override fun Updater.apply() {
        setOutput(PinRequirementOutput.Error(t))
    }
}
