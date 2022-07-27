package com.pexip.sdk.sample.welcome

import com.squareup.workflow1.ui.compose.composeViewFactory

val WelcomeViewFactory = composeViewFactory<WelcomeRendering> { rendering, _ ->
    WelcomeScreen(
        displayName = rendering.displayName,
        onDisplayNameChange = rendering.onDisplayNameChange,
        onNextClick = rendering.onNextClick,
        onBackClick = rendering.onBackClick
    )
}
