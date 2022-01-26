package com.pexip.sdk.video.sample.pin

import com.pexip.sdk.video.pin.PinRequirementRendering.Failure
import com.pexip.sdk.video.pin.PinRequirementRendering.ResolvingPinRequirement
import com.squareup.workflow1.ui.compose.composeViewFactory

object PinRequirementViewFactory {

    val ResolvingPinRequirementViewFactory = composeViewFactory<ResolvingPinRequirement> { _, _ ->
        ResolvingPinRequirementScreen()
    }

    val FailureViewFactory = composeViewFactory<Failure> { rendering, _ ->
        FailureScreen(rendering = rendering)
    }
}
