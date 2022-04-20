package com.pexip.sdk.video.sample.alias

import com.squareup.workflow1.ui.compose.composeViewFactory

val AliasViewFactory = composeViewFactory<AliasRendering> { rendering, _ ->
    AliasScreen(
        alias = rendering.alias,
        host = rendering.host,
        presentationInMain = rendering.presentationInMain,
        onAliasChange = rendering.onAliasChange,
        onHostChange = rendering.onHostChange,
        onPresentationInMainChange = rendering.onPresentationInMainChange,
        onResolveClick = rendering.onResolveClick,
        onBackClick = rendering.onBackClick
    )
}
