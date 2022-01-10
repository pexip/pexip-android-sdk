package com.pexip.sdk.video.pin.internal

import com.pexip.sdk.video.pin.PinRequirementRendering.Failure
import com.pexip.sdk.video.pin.PinRequirementRendering.ResolvingPinRequirement
import com.pexip.sdk.workflow.ui.ExperimentalWorkflowUiApi
import com.pexip.sdk.workflow.ui.renderer

@ExperimentalWorkflowUiApi
internal object PinRequirementRenderer {

    val ResolvingPinRequirementRenderer = renderer<ResolvingPinRequirement> {
        ResolvingPinRequirementScreen(modifier = it)
    }

    val FailureRenderer = renderer<Failure> {
        FailureScreen(rendering = this, modifier = it)
    }
}
