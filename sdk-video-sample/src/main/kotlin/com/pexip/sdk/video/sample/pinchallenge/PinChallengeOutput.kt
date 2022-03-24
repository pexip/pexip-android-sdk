package com.pexip.sdk.video.sample.pinchallenge

import com.pexip.sdk.video.api.RequestTokenResponse

sealed class PinChallengeOutput {

    data class Response(val response: RequestTokenResponse) : PinChallengeOutput()

    object Back : PinChallengeOutput() {

        override fun toString(): String = "Back"
    }
}
