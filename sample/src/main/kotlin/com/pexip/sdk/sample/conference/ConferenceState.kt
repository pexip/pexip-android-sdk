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
package com.pexip.sdk.sample.conference

import android.content.Intent
import com.pexip.sdk.conference.Message
import com.pexip.sdk.media.LocalVideoTrack
import com.pexip.sdk.media.MediaConnection
import com.pexip.sdk.media.VideoTrack
import kotlinx.coroutines.flow.MutableSharedFlow

data class ConferenceState(
    val connection: MediaConnection,
    val screenCaptureData: Intent? = null,
    val screenCaptureVideoTrack: LocalVideoTrack? = null,
    val screenCapturing: Boolean = screenCaptureVideoTrack?.capturing ?: false,
    val mainRemoteVideoTrack: VideoTrack? = connection.mainRemoteVideoTrack,
    val presentation: Boolean = false,
    val presentationRemoteVideoTrack: VideoTrack? = connection.presentationRemoteVideoTrack,
    val audioDevicesVisible: Boolean = false,
    val bandwidthVisible: Boolean = false,
    val dtmfVisible: Boolean = false,
    val showingChat: Boolean = false,
    val messages: List<Message> = emptyList(),
    val message: MutableSharedFlow<String> = MutableSharedFlow(extraBufferCapacity = 1),
)
