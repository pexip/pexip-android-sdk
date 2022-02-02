package com.pexip.sdk.video.sample.alias

data class AliasRendering(
    val alias: String,
    val onAliasChange: (String) -> Unit,
    val resolveEnabled: Boolean,
    val onResolveClick: () -> Unit,
    val onBackClick: () -> Unit,
)
