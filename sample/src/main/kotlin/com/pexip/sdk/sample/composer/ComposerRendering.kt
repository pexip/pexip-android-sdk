package com.pexip.sdk.sample.composer

data class ComposerRendering(
    val message: String,
    val submitEnabled: Boolean,
    val onMessageChange: (String) -> Unit,
    val onSubmitClick: () -> Unit,
)
