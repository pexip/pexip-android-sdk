package com.pexip.sdk.sample.pinrequirement

import com.pexip.sdk.sample.pinrequirement.PinRequirementRendering.Failure
import com.pexip.sdk.sample.pinrequirement.PinRequirementRendering.ResolvingPinRequirement
import com.squareup.workflow1.ui.compose.composeViewFactory

object PinRequirementViewFactory {

    val ResolvingPinRequirementViewFactory = composeViewFactory<ResolvingPinRequirement> { _, _ ->
        ResolvingPinRequirementScreen()
    }

    val FailureViewFactory = composeViewFactory<Failure> { rendering, _ ->
        FailureScreen(rendering = rendering)
    }
}
