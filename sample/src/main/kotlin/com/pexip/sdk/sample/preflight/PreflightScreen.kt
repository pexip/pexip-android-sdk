package com.pexip.sdk.sample.preflight

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContentPadding
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
import com.pexip.sdk.media.webrtc.compose.VideoTrackRenderer
import com.pexip.sdk.sample.CameraIconButton
import com.pexip.sdk.sample.IconButton
import com.pexip.sdk.sample.IconButtonDefaults
import com.pexip.sdk.sample.MicrophoneIconButton
import com.squareup.workflow1.ui.ViewEnvironment
import com.squareup.workflow1.ui.compose.WorkflowRendering

@Composable
fun PreflightScreen(
    rendering: PreflightRendering,
    environment: ViewEnvironment,
    modifier: Modifier = Modifier,
) {
    BackHandler(onBack = rendering.onBackClick)
    Surface(modifier = modifier) {
        Box(modifier = Modifier.fillMaxSize()) {
            VideoTrackRenderer(
                videoTrack = rendering.cameraVideoTrack?.takeIf {
                    rendering.cameraVideoTrackRendering?.capturing == true
                },
                mirror = true,
                modifier = Modifier.fillMaxSize()
            )
            if (rendering.cameraVideoTrack == null) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    Text(
                        text = "Camera has been disconnected",
                        color = Color.White
                    )
                    Button(onClick = rendering.onCreateCameraVideoTrackClick) {
                        Text(text = "Try again")
                    }
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .safeContentPadding()
                    .align(Alignment.BottomCenter)
            ) {
                CameraIconButton(rendering = rendering.cameraVideoTrackRendering)
                MicrophoneIconButton(rendering = rendering.microphoneAudioTrackRendering)
                Spacer(Modifier.weight(1f))
                IconButton(
                    onClick = rendering.onCallClick,
                    enabled = rendering.callEnabled,
                    colors = IconButtonDefaults.iconButtonColors(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = null
                    )
                }
            }
        }
    }
    if (rendering.childRendering != null) {
        WorkflowRendering(
            rendering = rendering.childRendering,
            viewEnvironment = environment,
        )
    }
}
