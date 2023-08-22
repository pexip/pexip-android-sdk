/*
 * Copyright 2021-2023 Pexip AS
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
import org.webrtc.GlRectDrawer

/**
 * Composes [SurfaceViewRenderer] and renders a [VideoTrack].
 *
 * Please note that changing [zOrderMediaOverlay] or [zOrderOnTop] after this
 * function was called has no effect.
 *
 * @param videoTrack an instance of video track or null
 * @param modifier optional [Modifier] to be applied to the video
 * @param keepScreenOn controls whether the screen should remain on
 * @param mirror defines if the video should rendered mirrored
 * @param zOrderMediaOverlay control whether the video is rendered on top of another video
 * @param zOrderOnTop control whether the video is rendered on top of its window. This overrides [zOrderMediaOverlay] if set
 */
@Composable
public fun VideoTrackRenderer(
    videoTrack: VideoTrack?,
    modifier: Modifier = Modifier,
    keepScreenOn: Boolean = true,
    mirror: Boolean = false,
    zOrderMediaOverlay: Boolean = false,
    zOrderOnTop: Boolean = false,
) {
    val eglBase = LocalEglBase.current
    val eglBaseContext = remember(eglBase) { eglBase?.eglBaseContext }
    val eglBaseConfigAttributes = LocalEglBaseConfigAttributes.current
    val context = LocalContext.current
    val renderer = remember(context) { SurfaceViewRenderer(context) }
    DisposableEffect(renderer, eglBaseContext) {
        renderer.init(eglBaseContext, null, eglBaseConfigAttributes, GlRectDrawer())
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
        factory = {
            renderer.apply {
                if (zOrderMediaOverlay) setZOrderMediaOverlay(true)
                if (zOrderOnTop) setZOrderOnTop(true)
            }
        },
        update = {
            renderer.apply { it.keepScreenOn = keepScreenOn }
        },
        modifier = modifier,
    )
}
