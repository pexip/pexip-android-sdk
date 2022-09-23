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
        modifier = modifier
    )
}
