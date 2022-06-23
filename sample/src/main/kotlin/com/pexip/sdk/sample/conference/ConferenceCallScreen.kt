package com.pexip.sdk.sample.conference

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Message
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.MicOff
import androidx.compose.material.icons.rounded.Pin
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material.icons.rounded.VideocamOff
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.pexip.sdk.media.webrtc.compose.VideoTrackRenderer
import com.pexip.sdk.sample.R
import com.pexip.sdk.sample.dtmf.DtmfDialog
import org.webrtc.EglBase

@Composable
fun ConferenceCallScreen(
    rendering: ConferenceCallRendering,
    eglBase: EglBase,
    modifier: Modifier = Modifier,
) {
    BackHandler(onBack = rendering.onBackClick)
    val sharedContext = remember(eglBase) { eglBase.eglBaseContext }
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxSize(),
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            VideoTrackRenderer(
                sharedContext = sharedContext,
                videoTrack = rendering.mainRemoteVideoTrack,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(ASPECT_RATIO_LANDSCAPE)
            )
            if (rendering.presentationRemoteVideoTrack != null) {
                VideoTrackRenderer(
                    sharedContext = sharedContext,
                    videoTrack = rendering.presentationRemoteVideoTrack,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(ASPECT_RATIO_LANDSCAPE)
                )
            }
        }
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            val aspectRatio = remember(maxWidth, maxHeight) {
                when {
                    maxWidth > maxHeight -> ASPECT_RATIO_LANDSCAPE
                    else -> ASPECT_RATIO_PORTRAIT
                }
            }
            AnimatedVisibility(
                visible = rendering.cameraCapturing,
                enter = slideInHorizontally { it * 2 },
                exit = slideOutHorizontally { it * 2 },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .fillMaxWidth(0.2f)
                    .aspectRatio(aspectRatio)
            ) {
                VideoTrackRenderer(
                    sharedContext = sharedContext,
                    videoTrack = rendering.cameraVideoTrack,
                    mirror = true
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
            ) {
                Button(onClick = rendering.onToggleBlur) {
                    Icon(
                        painter = painterResource(id = R.drawable.blur),
                        contentDescription = null
                    )
                }
                Button(onClick = rendering.onToggleLocalAudioCapturing) {
                    Icon(
                        imageVector = when (rendering.localAudioCapturing) {
                            true -> Icons.Rounded.Mic
                            false -> Icons.Rounded.MicOff
                        },
                        contentDescription = null
                    )
                }
                Button(onClick = rendering.onToggleCameraCapturing) {
                    Icon(
                        imageVector = when (rendering.cameraCapturing) {
                            true -> Icons.Rounded.Videocam
                            false -> Icons.Rounded.VideocamOff
                        },
                        contentDescription = null
                    )
                }
                Button(onClick = rendering.onConferenceEventsClick) {
                    Icon(
                        imageVector = Icons.Rounded.Message,
                        contentDescription = null
                    )
                }
                Button(onClick = rendering.onToggleDtmfClick) {
                    Icon(
                        imageVector = Icons.Rounded.Pin,
                        contentDescription = null
                    )
                }
            }
        }
    }
    if (rendering.dtmfRendering != null) {
        DtmfDialog(rendering = rendering.dtmfRendering)
    }
}

private const val ASPECT_RATIO_PORTRAIT = 9 / 16f
private const val ASPECT_RATIO_LANDSCAPE = 16 / 9f
