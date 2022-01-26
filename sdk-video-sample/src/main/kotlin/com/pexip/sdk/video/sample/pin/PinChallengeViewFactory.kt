package com.pexip.sdk.video.sample.pin

import com.pexip.sdk.video.pin.PinChallengeRendering
import com.squareup.workflow1.ui.compose.composeViewFactory

val PinChallengeViewFactory = composeViewFactory<PinChallengeRendering> { rendering, _ ->
    PinChallengeScreen(rendering = rendering)
}
