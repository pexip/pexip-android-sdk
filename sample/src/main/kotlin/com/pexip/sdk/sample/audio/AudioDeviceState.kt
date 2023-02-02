/*
 * Copyright 2022-2023 Pexip AS
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
import com.pexip.sdk.media.AudioDeviceManager

data class AudioDeviceState(
    val audioDeviceManager: AudioDeviceManager,
    val availableAudioDevices: List<AudioDevice>,
    val selectedAudioDevice: AudioDevice?,
) {

    constructor(audioDeviceManager: AudioDeviceManager) : this(
        audioDeviceManager = audioDeviceManager,
        availableAudioDevices = audioDeviceManager.availableAudioDevices,
        selectedAudioDevice = audioDeviceManager.selectedAudioDevice,
    )
}
