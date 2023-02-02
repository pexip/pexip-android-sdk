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

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BluetoothAudio
import androidx.compose.material.icons.rounded.HeadsetMic
import androidx.compose.material.icons.rounded.PhoneInTalk
import androidx.compose.material.icons.rounded.SpeakerPhone
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.pexip.sdk.media.AudioDevice

@Composable
fun AudioDeviceIcon(type: AudioDevice.Type, modifier: Modifier = Modifier) {
    Icon(
        imageVector = when (type) {
            AudioDevice.Type.BUILTIN_EARPIECE -> Icons.Rounded.PhoneInTalk
            AudioDevice.Type.BUILTIN_SPEAKER -> Icons.Rounded.SpeakerPhone
            AudioDevice.Type.BLUETOOTH_A2DP -> Icons.Rounded.BluetoothAudio
            AudioDevice.Type.BLUETOOTH_SCO -> Icons.Rounded.BluetoothAudio
            AudioDevice.Type.WIRED_HEADSET -> Icons.Rounded.HeadsetMic
        },
        contentDescription = null,
        modifier = modifier,
    )
}
