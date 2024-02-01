/*
 * Copyright 2024 Pexip AS
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
package com.pexip.sdk.media

fun audioDeviceManagerSample(manager: AudioDeviceManager) {
    // Get available audio devices
    // A listener-based variant is also available to observe the changes in the list
    val availableAudioDevices = manager.availableAudioDevices
    // Pick the preferred audio device from the list
    // The lower the value returned by minByOrNull, the higher the priority
    val preferredAudioDevice = availableAudioDevices.minByOrNull {
        when (it.type) {
            // Prefer SCO and wired headsets over any other audio device
            AudioDevice.Type.BLUETOOTH_SCO -> 0
            AudioDevice.Type.WIRED_HEADSET -> 1
            AudioDevice.Type.BUILTIN_SPEAKER -> 2
            AudioDevice.Type.BUILTIN_EARPIECE -> 3
            // Assign the lowest priority for A2DP since it's not suitable for VoIP
            AudioDevice.Type.BLUETOOTH_A2DP -> Int.MAX_VALUE
        }
    }
    // Ensure that there is a preferred audio device and select it
    if (preferredAudioDevice != null) {
        manager.selectAudioDevice(preferredAudioDevice)
    }
    // Once the call is finished, make sure to dispose of the AudioDeviceManager to release any
    // resources held by it
    manager.dispose()
}
