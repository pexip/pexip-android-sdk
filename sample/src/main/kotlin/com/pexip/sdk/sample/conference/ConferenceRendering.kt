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
import com.pexip.sdk.conference.SplashScreen
import com.pexip.sdk.media.VideoTrack
import com.pexip.sdk.sample.audio.AudioDeviceRendering
import com.pexip.sdk.sample.bandwidth.BandwidthRendering
import com.pexip.sdk.sample.dtmf.DtmfRendering
import com.pexip.sdk.sample.media.LocalMediaTrackRendering

data class ConferenceRendering(
    val splashScreen: SplashScreen?,
    val cameraVideoTrack: VideoTrack?,
    val mainRemoteVideoTrack: VideoTrack?,
    val presentationRemoteVideoTrack: VideoTrack?,
    val audioDeviceRendering: AudioDeviceRendering,
    val bandwidthRendering: BandwidthRendering,
    val dtmfRendering: DtmfRendering,
    val cameraVideoTrackRendering: LocalMediaTrackRendering?,
    val microphoneAudioTrackRendering: LocalMediaTrackRendering?,
    val screenCapturing: Boolean,
    val onScreenCapture: (Intent) -> Unit,
    val onAspectRatioChange: (Float) -> Unit,
    val onAudioDevicesChange: (Boolean) -> Unit,
    val onBandwidthChange: (Boolean) -> Unit,
    val onDtmfChange: (Boolean) -> Unit,
    val onStopScreenCapture: () -> Unit,
    val onChatClick: () -> Unit,
    val onBackClick: () -> Unit,
)
