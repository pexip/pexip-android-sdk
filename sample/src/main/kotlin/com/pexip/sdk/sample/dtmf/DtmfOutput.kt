package com.pexip.sdk.sample.dtmf

sealed class DtmfOutput {

    class Tone(val tone: String) : DtmfOutput()

    object Back : DtmfOutput() {

        override fun toString(): String = "Back"
    }
}
