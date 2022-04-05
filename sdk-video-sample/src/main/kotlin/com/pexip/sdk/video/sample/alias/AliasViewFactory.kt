package com.pexip.sdk.video.sample.alias

import com.squareup.workflow1.ui.compose.composeViewFactory

val AliasViewFactory = composeViewFactory<AliasRendering> { rendering, _ ->
    AliasScreen(
        alias = rendering.alias,
        host = rendering.host,
        onAliasChange = rendering.onAliasChange,
        onHostChange = rendering.onHostChange,
        onResolveClick = rendering.onResolveClick,
        onBackClick = rendering.onBackClick
    )
}
