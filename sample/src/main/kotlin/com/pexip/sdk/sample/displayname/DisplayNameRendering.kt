package com.pexip.sdk.sample.displayname

data class DisplayNameRendering(
    val displayName: String,
    val onDisplayNameChange: (String) -> Unit,
    val onNextClick: () -> Unit,
    val onBackClick: () -> Unit,
)
