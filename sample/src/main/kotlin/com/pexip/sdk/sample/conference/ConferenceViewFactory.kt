package com.pexip.sdk.sample.conference

import com.pexip.sdk.sample.EglBaseKey
import com.squareup.workflow1.ui.compose.composeViewFactory

object ConferenceViewFactory {

    val ConferenceCallViewFactory =
        composeViewFactory<ConferenceCallRendering> { rendering, environment ->
            val eglBase = environment[EglBaseKey]
            ConferenceCallScreen(rendering, eglBase)
        }

    val ConferenceEventsViewFactory =
        composeViewFactory<ConferenceEventsRendering> { rendering, _ ->
            ConferenceEventsScreen(rendering)
        }
}
