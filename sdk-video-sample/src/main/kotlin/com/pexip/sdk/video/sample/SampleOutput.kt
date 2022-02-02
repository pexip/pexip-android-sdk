package com.pexip.sdk.video.sample

sealed class SampleOutput {

    data class Toast(val message: String) : SampleOutput()
    object Finish : SampleOutput()
}
