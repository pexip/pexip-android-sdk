/*
 * Copyright 2022 Pexip AS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
