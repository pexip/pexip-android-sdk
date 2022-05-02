package com.pexip.sdk.video.sample.conference

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import org.webrtc.EglBase
import org.webrtc.GlRectDrawer
import org.webrtc.SurfaceViewRenderer
import org.webrtc.VideoTrack

@Composable
internal fun VideoRenderer(
    sharedContext: EglBase.Context,
    videoTrack: VideoTrack?,
    modifier: Modifier = Modifier,
    mirror: Boolean = false,
) {
    val context = LocalContext.current
    val renderer = remember(context) { SurfaceViewRenderer(context) }
    DisposableEffect(renderer, sharedContext) {
        renderer.init(sharedContext, null, EglBase.CONFIG_PLAIN, GlRectDrawer())
        onDispose {
            renderer.clearImage()
            renderer.release()
        }
    }
    LaunchedEffect(renderer, mirror) {
        renderer.setMirror(mirror)
    }
    DisposableEffect(renderer, videoTrack) {
        videoTrack?.addSink(renderer)
        onDispose {
            videoTrack?.removeSink(renderer)
            renderer.clearImage()
        }
    }
    AndroidView(
        factory = { renderer.apply { keepScreenOn = true } },
        modifier = modifier
    )
}
