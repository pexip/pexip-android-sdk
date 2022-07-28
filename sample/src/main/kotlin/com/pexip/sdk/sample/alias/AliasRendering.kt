package com.pexip.sdk.sample.alias

data class AliasRendering(
    val alias: String,
    val host: String,
    val presentationInMain: Boolean,
    val resolveEnabled: Boolean,
    val onAliasChange: (String) -> Unit,
    val onHostChange: (String) -> Unit,
    val onPresentationInMainChange: (Boolean) -> Unit,
    val onResolveClick: () -> Unit,
    val onBackClick: () -> Unit,
)
