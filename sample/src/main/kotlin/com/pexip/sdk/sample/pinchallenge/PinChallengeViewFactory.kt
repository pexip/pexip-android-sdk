package com.pexip.sdk.sample.pinchallenge

import com.squareup.workflow1.ui.compose.composeViewFactory

val PinChallengeViewFactory = composeViewFactory<PinChallengeRendering> { rendering, _ ->
    PinChallengeScreen(rendering = rendering)
}
