package com.pexip.sdk.sample.alias

import com.squareup.workflow1.ui.compose.composeViewFactory

val AliasViewFactory = composeViewFactory<AliasRendering> { rendering, _ ->
    AliasScreen(
        alias = rendering.alias,
        host = rendering.host,
        presentationInMain = rendering.presentationInMain,
        resolveEnabled = rendering.resolveEnabled,
        onAliasChange = rendering.onAliasChange,
        onHostChange = rendering.onHostChange,
        onPresentationInMainChange = rendering.onPresentationInMainChange,
        onResolveClick = rendering.onResolveClick,
        onBackClick = rendering.onBackClick
    )
}
