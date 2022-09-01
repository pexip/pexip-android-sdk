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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.pexip.sdk.media.AudioDevice

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AudioDeviceDialog(rendering: AudioDeviceRendering) {
    if (rendering.visible) {
        Dialog(
            onDismissRequest = rendering.onBackClick,
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(shape = Shape, modifier = Modifier.fillMaxWidth(0.8f)) {
                AudioDeviceList(
                    availableAudioDevices = rendering.availableAudioDevices,
                    selectedAudioDevice = rendering.selectedAudioDevice,
                    onAudioDeviceClick = rendering.onAudioDeviceClick
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
                onAudioDeviceClick = onAudioDeviceClick
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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
        headlineText = {
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
        }
    )
}

private val Shape = RoundedCornerShape(8.dp)
private val ContentPadding = PaddingValues(vertical = 8.dp)
