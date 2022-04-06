package com.pexip.sdk.video.sample.conference

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material.icons.rounded.VideocamOff
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pexip.libwebrtc.compose.VideoRenderer

@Composable
fun ConferenceScreen(rendering: ConferenceRendering, modifier: Modifier = Modifier) {
    BackHandler(onBack = rendering.onBackClick)
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxSize(),
    ) {
        VideoRenderer(
            videoTrack = rendering.remoteVideoTrack,
            sharedContext = rendering.sharedContext,
            aspectRatio = ASPECT_RATIO_LANDSCAPE,
            modifier = Modifier.fillMaxSize()
        )
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
                        videoTrack = rendering.localVideoTrack,
                        sharedContext = rendering.sharedContext,
                        aspectRatio = remember(maxWidth, maxHeight) {
                            when {
                                maxWidth > maxHeight -> ASPECT_RATIO_LANDSCAPE
                                else -> ASPECT_RATIO_PORTRAIT
                            }
                        }
                    )
                }
            }
            Button(
                onClick = rendering.onToggleMainCapturing,
                modifier = Modifier.align(Alignment.BottomStart)
            ) {
                Icon(
                    imageVector = when (rendering.mainCapturing) {
                        true -> Icons.Rounded.Videocam
                        false -> Icons.Rounded.VideocamOff
                    },
                    contentDescription = null
                )
            }
        }
    }
}

private val SelfViewShape = RoundedCornerShape(4.dp)
private const val ASPECT_RATIO_PORTRAIT = 9 / 16f
private const val ASPECT_RATIO_LANDSCAPE = 16 / 9f
