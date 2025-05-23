/*
 * Copyright 2022-2025 Pexip AS
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

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.pexip.sdk.media.AudioDevice
import com.squareup.workflow1.ui.Screen

data class AudioDeviceScreen(
    val visible: Boolean,
    val availableAudioDevices: List<AudioDevice>,
    val selectedAudioDevice: AudioDevice?,
    val onAudioDeviceClick: (AudioDevice) -> Unit,
    val onBackClick: () -> Unit,
) : Screen

@Composable
fun AudioDeviceDialog(screen: AudioDeviceScreen) {
    if (screen.visible) {
        Dialog(
            onDismissRequest = screen.onBackClick,
            properties = DialogProperties(usePlatformDefaultWidth = false),
        ) {
            Surface(shape = Shape, modifier = Modifier.fillMaxWidth(0.8f)) {
                AudioDeviceList(
                    availableAudioDevices = screen.availableAudioDevices,
                    selectedAudioDevice = screen.selectedAudioDevice,
                    onAudioDeviceClick = screen.onAudioDeviceClick,
                )
            }
        }
    }
}

@Composable
private fun AudioDeviceList(
    availableAudioDevices: List<AudioDevice>,
    selectedAudioDevice: AudioDevice?,
    onAudioDeviceClick: (AudioDevice) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(contentPadding = ContentPadding, modifier = modifier.selectableGroup()) {
        items(availableAudioDevices) {
            AudioDevice(
                audioDevice = it,
                selected = it == selectedAudioDevice,
                onAudioDeviceClick = onAudioDeviceClick,
            )
        }
    }
}

@Composable
private fun AudioDevice(
    audioDevice: AudioDevice,
    selected: Boolean,
    onAudioDeviceClick: (AudioDevice) -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentOnAudioDeviceClick by rememberUpdatedState(onAudioDeviceClick)
    ListItem(
        leadingContent = {
            AudioDeviceIcon(audioDevice.type)
        },
        headlineContent = {
            val text = remember(audioDevice) {
                when (audioDevice.type) {
                    AudioDevice.Type.BUILTIN_EARPIECE -> "Earpiece"
                    AudioDevice.Type.BUILTIN_SPEAKER -> "Speaker"
                    AudioDevice.Type.WIRED_HEADSET -> "Headset"
                    AudioDevice.Type.BLUETOOTH_A2DP, AudioDevice.Type.BLUETOOTH_SCO -> {
                        audioDevice.name ?: "Bluetooth"
                    }
                }
            }
            Text(text = text)
        },
        trailingContent = when (selected) {
            true -> {
                {
                    Icon(imageVector = Icons.Rounded.Check, contentDescription = null)
                }
            }
            else -> null
        },
        modifier = modifier.selectable(selected = selected) {
            currentOnAudioDeviceClick(audioDevice)
        },
    )
}

private val Shape = RoundedCornerShape(8.dp)
private val ContentPadding = PaddingValues(vertical = 8.dp)
