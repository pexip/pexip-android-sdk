package com.pexip.sdk.video.sample.conference

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pexip.sdk.video.sample.VideoRenderer

@Composable
fun ConferenceScreen(rendering: ConferenceRendering, modifier: Modifier = Modifier) {
    BackHandler(onBack = rendering.onBackClick)
    BoxWithConstraints(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.primary),
    ) {
        VideoRenderer(
            track = rendering.remoteVideoTrack,
            aspectRatio = ASPECT_RATIO_LANDSCAPE,
            modifier = Modifier.fillMaxSize()
        )
        Surface(
            shape = SelfViewShape,
            elevation = 4.dp,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
                .fillMaxWidth(0.2f)
        ) {
            val aspectRatio = remember(maxWidth, maxHeight) {
                if (maxWidth > maxHeight) ASPECT_RATIO_LANDSCAPE else ASPECT_RATIO_PORTRAIT
            }
            VideoRenderer(
                track = rendering.localVideoTrack,
                aspectRatio = aspectRatio
            )
        }
    }
}

private val SelfViewShape = RoundedCornerShape(4.dp)
private const val ASPECT_RATIO_PORTRAIT = 9 / 16f
private const val ASPECT_RATIO_LANDSCAPE = 16 / 9f
