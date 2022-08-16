package com.pexip.sdk.sample.conference

import com.squareup.workflow1.ui.compose.composeViewFactory

object ConferenceViewFactory {

    val ConferenceCallViewFactory = composeViewFactory<ConferenceCallRendering> { rendering, _ ->
        ConferenceCallScreen(rendering)
    }

    val ConferenceEventsViewFactory =
        composeViewFactory<ConferenceEventsRendering> { rendering, _ ->
            ConferenceEventsScreen(rendering)
        }
}
