/*
 * Copyright 2022-2023 Pexip AS
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
package com.pexip.sdk.media.webrtc.internal

import com.pexip.sdk.media.Renderer
import com.pexip.sdk.media.VideoTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.webrtc.VideoSink

internal class WebRtcVideoTrack(
    private val videoTrack: org.webrtc.VideoTrack,
    private val scope: CoroutineScope,
) : VideoTrack {

    override fun addRenderer(renderer: Renderer) {
        require(renderer is VideoSink) { "renderer must be an instance of VideoSink." }
        scope.launch { videoTrack.addSink(renderer) }
    }

    override fun removeRenderer(renderer: Renderer) {
        require(renderer is VideoSink) { "renderer must be an instance of VideoSink." }
        scope.launch { videoTrack.removeSink(renderer) }
    }
}
