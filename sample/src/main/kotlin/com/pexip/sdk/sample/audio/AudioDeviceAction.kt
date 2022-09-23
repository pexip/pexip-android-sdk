package com.pexip.sdk.sample.audio

import com.pexip.sdk.media.AudioDevice
import com.squareup.workflow1.WorkflowAction

typealias AudioDeviceAction = WorkflowAction<AudioDeviceProps, AudioDeviceState, AudioDeviceOutput>

data class OnAvailableAudioDevicesChange(val availableAudioDevices: List<AudioDevice>) :
    AudioDeviceAction() {

    override fun Updater.apply() {
        state = state.copy(availableAudioDevices = availableAudioDevices)
    }
}

data class OnSelectedAudioDeviceChange(val selectedAudioDevice: AudioDevice?) :
    AudioDeviceAction() {

    override fun Updater.apply() {
        state = state.copy(selectedAudioDevice = selectedAudioDevice)
    }
}

data class OnAudioDeviceClick(val audioDevice: AudioDevice) : AudioDeviceAction() {

    override fun Updater.apply() {
        state.audioDeviceManager.selectAudioDevice(audioDevice)
    }
}

class OnBackClick : AudioDeviceAction() {

    override fun Updater.apply() {
        setOutput(AudioDeviceOutput.Back)
    }
}
