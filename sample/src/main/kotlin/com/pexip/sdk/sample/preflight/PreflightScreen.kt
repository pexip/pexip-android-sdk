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
package com.pexip.sdk.sample.preflight

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.pexip.sdk.media.VideoTrack
import com.pexip.sdk.media.webrtc.compose.VideoTrackRenderer
import com.pexip.sdk.sample.CameraIconButton
import com.pexip.sdk.sample.IconButton
import com.pexip.sdk.sample.IconButtonDefaults
import com.pexip.sdk.sample.MicrophoneIconButton
import com.pexip.sdk.sample.media.LocalMediaTrackRendering
import com.squareup.workflow1.ui.Screen
import com.squareup.workflow1.ui.compose.WorkflowRendering
import org.webrtc.RendererCommon

data class PreflightScreen(
    val childRendering: Screen?,
    val cameraVideoTrack: VideoTrack?,
    val callEnabled: Boolean,
    val onCallClick: () -> Unit,
    val onCreateCameraVideoTrackClick: () -> Unit,
    val cameraVideoTrackRendering: LocalMediaTrackRendering?,
    val microphoneAudioTrackRendering: LocalMediaTrackRendering?,
    val onBackClick: () -> Unit,
) : Screen

@Composable
fun PreflightScreen(screen: PreflightScreen, modifier: Modifier = Modifier) {
    BackHandler(onBack = screen.onBackClick)
    Surface(modifier = modifier) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            VideoTrackRenderer(
                videoTrack = screen.cameraVideoTrack?.takeIf {
                    screen.cameraVideoTrackRendering?.capturing == true
                },
                mirror = true,
                scalingTypeMatchOrientation = RendererCommon.ScalingType.SCALE_ASPECT_FIT,
                modifier = Modifier.wrapContentSize(),
            )
            if (screen.cameraVideoTrack == null) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.align(Alignment.Center),
                ) {
                    Text(
                        text = "Camera has been disconnected",
                        color = Color.White,
                    )
                    Button(onClick = screen.onCreateCameraVideoTrackClick) {
                        Text(text = "Try again")
                    }
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .safeContentPadding()
                    .align(Alignment.BottomCenter),
            ) {
                CameraIconButton(rendering = screen.cameraVideoTrackRendering)
                MicrophoneIconButton(rendering = screen.microphoneAudioTrackRendering)
                Spacer(Modifier.weight(1f))
                IconButton(
                    onClick = screen.onCallClick,
                    enabled = screen.callEnabled,
                    colors = IconButtonDefaults.iconButtonColors(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    ),
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = null,
                    )
                }
            }
        }
    }
    if (screen.childRendering != null) {
        WorkflowRendering(rendering = screen.childRendering)
    }
}
