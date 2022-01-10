package com.pexip.sdk.sample.conference

sealed class ConferenceOutput {

    object Back : ConferenceOutput() {

        override fun toString(): String = "Back"
    }
}
