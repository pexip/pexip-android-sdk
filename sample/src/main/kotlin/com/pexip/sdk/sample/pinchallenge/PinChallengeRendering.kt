package com.pexip.sdk.sample.pinchallenge

data class PinChallengeRendering(
    val pin: String,
    val error: Boolean,
    val submitEnabled: Boolean,
    val onPinChange: (String) -> Unit,
    val onSubmitClick: () -> Unit,
    val onBackClick: () -> Unit,
)
