package com.pexip.sdk.sample.audio

sealed class AudioDeviceOutput {

    object Back : AudioDeviceOutput() {

        override fun toString(): String = "Back"
    }
}
