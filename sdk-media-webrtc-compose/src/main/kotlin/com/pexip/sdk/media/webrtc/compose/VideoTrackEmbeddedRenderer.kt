/*
 * Copyright 2024 Pexip AS
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

import androidx.compose.foundation.AndroidEmbeddedExternalSurface
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.pexip.sdk.media.Renderer
import com.pexip.sdk.media.VideoTrack
import org.webrtc.GlRectDrawer
import org.webrtc.RendererCommon
import org.webrtc.ThreadUtils
import java.util.concurrent.CountDownLatch

/**
 * Renders a [VideoTrack] embedded directly in the UI hierarchy.
 *
 * Unlike [VideoTrackRenderer] it positions its surface as a regular element inside
 * the composable hierarchy and seamlessly supports clipping.
 *
 * @param videoTrack an instance of video track or null
 * @param modifier optional [Modifier] to be applied to the video
 * @param mirror defines if the video should rendered mirrored
 */
@Composable
public fun VideoTrackEmbeddedRenderer(
    videoTrack: VideoTrack?,
    modifier: Modifier = Modifier,
    mirror: Boolean = false,
) {
    val eglBase = LocalEglBase.current
    val eglBaseContext = remember(eglBase) { eglBase?.eglBaseContext }
    val eglBaseConfigAttributes = LocalEglBaseConfigAttributes.current
    val renderer = remember { SurfaceEglRenderer("VideoTrackEmbeddedRenderer") }
    var aspectRatio by remember { mutableFloatStateOf(0f) }
    DisposableEffect(renderer, eglBaseContext) {
        val events = object : RendererCommon.RendererEvents {

            override fun onFirstFrameRendered() = Unit

            override fun onFrameResolutionChanged(width: Int, height: Int, rotation: Int) {
                aspectRatio = when (rotation % 180) {
                    0 -> width / height.toFloat()
                    else -> height / width.toFloat()
                }
            }
        }
        renderer.init(eglBaseContext, events, eglBaseConfigAttributes, GlRectDrawer())
        onDispose {
            renderer.clearImage()
            renderer.release()
        }
    }
    DisposableEffect(renderer, videoTrack) {
        videoTrack?.addRenderer(renderer)
        onDispose {
            videoTrack?.removeRenderer(renderer)
            renderer.clearImage()
        }
    }
    LaunchedEffect(renderer, mirror) {
        renderer.setMirror(mirror)
    }
    LaunchedEffect(renderer, aspectRatio) {
        renderer.setLayoutAspectRatio(aspectRatio)
    }
    if (aspectRatio != 0f) {
        AndroidEmbeddedExternalSurface(modifier = modifier.aspectRatio(aspectRatio)) {
            onSurface { surface, _, _ ->
                renderer.createEglSurface(surface)
                surface.onDestroyed {
                    val latch = CountDownLatch(1)
                    renderer.releaseEglSurface(latch::countDown)
                    ThreadUtils.awaitUninterruptibly(latch)
                }
            }
        }
    }
}

private class SurfaceEglRenderer(name: String) :
    org.webrtc.SurfaceEglRenderer(name),
    Renderer
