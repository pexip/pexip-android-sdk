package com.pexip.sdk.video.sample.alias

data class AliasRendering(
    val alias: String,
    val host: String,
    val onAliasChange: (String) -> Unit,
    val onHostChange: (String) -> Unit,
    val onResolveClick: () -> Unit,
    val onBackClick: () -> Unit,
)
