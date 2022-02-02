package com.pexip.sdk.video.sample.pinchallenge

sealed class PinChallengeOutput {

    data class Token(val token: com.pexip.sdk.video.api.Token) : PinChallengeOutput()

    object Back : PinChallengeOutput() {

        override fun toString(): String = "Back"
    }
}
