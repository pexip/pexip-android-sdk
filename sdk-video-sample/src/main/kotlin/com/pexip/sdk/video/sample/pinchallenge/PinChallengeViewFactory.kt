package com.pexip.sdk.video.sample.pinchallenge

import com.squareup.workflow1.ui.compose.composeViewFactory

val PinChallengeViewFactory = composeViewFactory<PinChallengeRendering> { rendering, _ ->
    PinChallengeScreen(rendering = rendering)
}
