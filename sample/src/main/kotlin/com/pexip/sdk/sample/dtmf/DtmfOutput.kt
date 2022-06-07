package com.pexip.sdk.sample.dtmf

sealed class DtmfOutput {

    object Back : DtmfOutput() {

        override fun toString(): String = "Back"
    }
}
