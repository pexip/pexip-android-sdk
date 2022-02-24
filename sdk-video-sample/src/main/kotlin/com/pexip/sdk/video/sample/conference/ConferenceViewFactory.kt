package com.pexip.sdk.video.sample.conference

import com.squareup.workflow1.ui.compose.composeViewFactory

val ConferenceViewFactory = composeViewFactory<ConferenceRendering> { rendering, _ ->
    ConferenceScreen(rendering)
}
