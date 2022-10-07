package com.pexip.sdk.sample

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.pexip.sdk.sample.media.LocalMediaTrackRendering

@Composable
fun LocalMediaTrackIconButton(
    rendering: LocalMediaTrackRendering?,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    IconToggleButton(
        enabled = rendering != null,
        checked = !(rendering?.capturing ?: true),
        onCheckedChange = { rendering?.onCapturingChange?.invoke(!it) },
        modifier = modifier,
        content = content
    )
}

@Composable
fun CameraIconButton(rendering: LocalMediaTrackRendering?, modifier: Modifier = Modifier) {
    LocalMediaTrackIconButton(rendering = rendering, modifier = modifier) {
        Icon(
            imageVector = when (rendering?.capturing ?: true) {
                true -> Icons.Default.Videocam
                else -> Icons.Default.VideocamOff
            },
            contentDescription = null
        )
    }
}

@Composable
fun MicrophoneIconButton(
    rendering: LocalMediaTrackRendering?,
    modifier: Modifier = Modifier,
) {
    LocalMediaTrackIconButton(rendering = rendering, modifier = modifier) {
        Icon(
            imageVector = when (rendering?.capturing ?: true) {
                true -> Icons.Default.Mic
                else -> Icons.Default.MicOff
            },
            contentDescription = null
        )
    }
}
