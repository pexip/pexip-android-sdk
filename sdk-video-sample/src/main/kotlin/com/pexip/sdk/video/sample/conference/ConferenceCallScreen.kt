package com.pexip.sdk.video.sample.conference

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Message
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material.icons.rounded.VideocamOff
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pexip.libwebrtc.compose.VideoRenderer

@Composable
fun ConferenceCallScreen(rendering: ConferenceCallRendering, modifier: Modifier = Modifier) {
    BackHandler(onBack = rendering.onBackClick)
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxSize(),
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            VideoRenderer(
                videoTrack = rendering.mainRemoteVideoTrack,
                sharedContext = rendering.sharedContext,
                aspectRatio = ASPECT_RATIO_LANDSCAPE,
                modifier = Modifier.fillMaxWidth()
            )
            if (rendering.presentationRemoteVideoTrack != null) {
                VideoRenderer(
                    videoTrack = rendering.presentationRemoteVideoTrack,
                    sharedContext = rendering.sharedContext,
                    aspectRatio = ASPECT_RATIO_LANDSCAPE,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            AnimatedVisibility(
                visible = rendering.mainCapturing,
                enter = slideInHorizontally { it * 2 },
                exit = slideOutHorizontally { it * 2 },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .fillMaxWidth(0.2f)
            ) {
                Surface(shape = SelfViewShape, elevation = 4.dp) {
                    VideoRenderer(
                        videoTrack = rendering.mainLocalVideoTrack,
                        sharedContext = rendering.sharedContext,
                        mirror = true,
                        aspectRatio = remember(maxWidth, maxHeight) {
                            when {
                                maxWidth > maxHeight -> ASPECT_RATIO_LANDSCAPE
                                else -> ASPECT_RATIO_PORTRAIT
                            }
                        }
                    )
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
            ) {
                Button(onClick = rendering.onToggleMainCapturing) {
                    Icon(
                        imageVector = when (rendering.mainCapturing) {
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
            }
        }
    }
}

private val SelfViewShape = RoundedCornerShape(4.dp)
private const val ASPECT_RATIO_PORTRAIT = 9 / 16f
private const val ASPECT_RATIO_LANDSCAPE = 16 / 9f
