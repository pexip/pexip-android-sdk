package com.pexip.sdk.sample.pinchallenge

data class PinChallengeState(
    val pin: String = "",
    val t: Throwable? = null,
    val requesting: Boolean = false,
    val pinToSubmit: String? = null,
)
