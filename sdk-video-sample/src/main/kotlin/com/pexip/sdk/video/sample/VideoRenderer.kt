package com.pexip.sdk.video.sample

import android.view.TextureView
import android.view.ViewGroup
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.pexip.sdk.video.conference.SurfaceTextureRenderer
import com.pexip.sdk.video.conference.VideoTrack

@Composable
fun VideoRenderer(
    track: VideoTrack?,
    modifier: Modifier = Modifier,
    mirror: Boolean = false,
    aspectRatio: Float = 0f,
) {
    val renderer = remember { SurfaceTextureRenderer("VideoRenderer") }
    LaunchedEffect(mirror) {
        renderer.setMirror(mirror)
    }
    LaunchedEffect(aspectRatio) {
        renderer.setLayoutAspectRatio(aspectRatio)
    }
    if (track != null) DisposableEffect(track) {
        renderer.init(track)
        onDispose(renderer::release)
    }
    AndroidView(
        factory = {
            TextureView(it).apply {
                keepScreenOn = true
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                surfaceTextureListener = renderer
            }
        },
        modifier = when (aspectRatio) {
            0f -> modifier
            else -> modifier.aspectRatio(aspectRatio)
        }
    )
}
