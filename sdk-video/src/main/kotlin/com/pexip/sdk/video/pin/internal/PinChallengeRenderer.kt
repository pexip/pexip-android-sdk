package com.pexip.sdk.video.pin.internal

import com.pexip.sdk.video.pin.PinChallengeRendering
import com.pexip.sdk.workflow.ui.ExperimentalWorkflowUiApi
import com.pexip.sdk.workflow.ui.renderer

@ExperimentalWorkflowUiApi
internal val PinChallengeRenderer = renderer<PinChallengeRendering> {
    PinChallengeScreen(rendering = this, modifier = it)
}
