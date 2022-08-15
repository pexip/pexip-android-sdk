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
    videoTrack: VideoTrack?,
    modifier: Modifier = Modifier,
    mirror: Boolean = false,
) {
    val eglBase = LocalEglBase.current
    val eglBaseContext = remember(eglBase) { eglBase?.eglBaseContext }
    VideoTrackRenderer(
        sharedContext = eglBaseContext,
        videoTrack = videoTrack,
        modifier = modifier,
        mirror = mirror,
    )
}

@Deprecated(
    "Use EglBase.Context-less version and use LocalEglBase to provide an EglBase instance.",
    ReplaceWith(
        expression = "VideoTrackRenderer(videoTrack, modifier, mirror)",
        imports = ["com.pexip.sdk.media.webrtc.compose.VideoTrackRenderer"]
    )
)
@Composable
public fun VideoTrackRenderer(
    sharedContext: EglBase.Context?,
    videoTrack: VideoTrack?,
    modifier: Modifier = Modifier,
    mirror: Boolean = false,
    configAttributes: IntArray = LocalEglBaseConfigAttributes.current,
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
