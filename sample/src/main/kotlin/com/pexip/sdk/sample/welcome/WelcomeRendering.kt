package com.pexip.sdk.sample.welcome

data class WelcomeRendering(
    val displayName: String,
    val onDisplayNameChange: (String) -> Unit,
    val onNextClick: () -> Unit,
    val onBackClick: () -> Unit,
)
