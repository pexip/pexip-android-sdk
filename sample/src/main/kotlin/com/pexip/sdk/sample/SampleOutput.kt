package com.pexip.sdk.sample

sealed class SampleOutput {

    data class Toast(val message: String) : SampleOutput()
    object ApplicationDetailsSettings : SampleOutput()
    object Finish : SampleOutput()
}
