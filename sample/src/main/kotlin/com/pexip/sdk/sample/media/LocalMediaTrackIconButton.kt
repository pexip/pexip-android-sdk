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
        content = content,
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
            contentDescription = null,
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
            contentDescription = null,
        )
    }
}
