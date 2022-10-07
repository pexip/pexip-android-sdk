package com.pexip.sdk.sample.conference

sealed interface ConferenceOutput {

    object Back : ConferenceOutput {

        override fun toString(): String = "Back"
    }
}
