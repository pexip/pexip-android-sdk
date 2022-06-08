package com.pexip.sdk.media.webrtc.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.pexip.sdk.media.VideoTrack
import com.pexip.sdk.media.webrtc.SurfaceViewRenderer
import org.webrtc.EglBase
import org.webrtc.GlRectDrawer

@Composable
public fun VideoTrackRenderer(
    sharedContext: EglBase.Context?,
    videoTrack: VideoTrack?,
    modifier: Modifier = Modifier,
    mirror: Boolean = false,
    configAttributes: IntArray = EglBase.CONFIG_PLAIN,
) {
    val context = LocalContext.current
    val renderer = remember(context) { SurfaceViewRenderer(context) }
    DisposableEffect(renderer, sharedContext) {
        renderer.init(sharedContext, null, configAttributes, GlRectDrawer())
        onDispose {
            renderer.clearImage()
            renderer.release()
        }
    }
    LaunchedEffect(renderer, mirror) {
        renderer.setMirror(mirror)
    }
    DisposableEffect(renderer, videoTrack) {
        videoTrack?.addRenderer(renderer)
        onDispose {
            videoTrack?.removeRenderer(renderer)
            renderer.clearImage()
        }
    }
    AndroidView(
        factory = { renderer.apply { keepScreenOn = true } },
        modifier = modifier
    )
}
