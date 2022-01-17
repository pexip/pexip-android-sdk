package com.pexip.sdk.video.pin.internal

import com.pexip.sdk.video.pin.PinChallengeRendering
import com.squareup.workflow1.ui.compose.composeViewFactory

internal val PinChallengeViewFactory = composeViewFactory<PinChallengeRendering> { rendering, _ ->
    PinChallengeScreen(rendering = rendering)
}
