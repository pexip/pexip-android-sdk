package com.pexip.sdk.sample.audio

import com.pexip.sdk.media.AudioDevice

data class AudioDeviceRendering(
    val visible: Boolean,
    val availableAudioDevices: List<AudioDevice>,
    val selectedAudioDevice: AudioDevice?,
    val onAudioDeviceClick: (AudioDevice) -> Unit,
    val onBackClick: () -> Unit,
)
