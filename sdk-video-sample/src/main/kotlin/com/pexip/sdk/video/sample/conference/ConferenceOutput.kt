package com.pexip.sdk.video.sample.conference

sealed class ConferenceOutput {

    object Back : ConferenceOutput() {

        override fun toString(): String = "Back"
    }
}
