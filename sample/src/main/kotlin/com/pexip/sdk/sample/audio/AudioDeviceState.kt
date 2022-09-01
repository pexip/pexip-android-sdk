package com.pexip.sdk.sample.audio

import com.pexip.sdk.media.AudioDevice
import com.pexip.sdk.media.AudioDeviceManager

data class AudioDeviceState(
    val audioDeviceManager: AudioDeviceManager,
    val availableAudioDevices: List<AudioDevice>,
    val selectedAudioDevice: AudioDevice?,
) {

    constructor(audioDeviceManager: AudioDeviceManager) : this(
        audioDeviceManager = audioDeviceManager,
        availableAudioDevices = audioDeviceManager.availableAudioDevices,
        selectedAudioDevice = audioDeviceManager.selectedAudioDevice
    )
}
