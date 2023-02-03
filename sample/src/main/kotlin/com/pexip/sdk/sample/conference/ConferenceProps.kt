/*
 * Copyright 2022 Pexip AS
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

import com.pexip.sdk.api.infinity.RequestTokenResponse
import com.pexip.sdk.media.CameraVideoTrack
import com.pexip.sdk.media.LocalAudioTrack
import java.net.URL

data class ConferenceProps(
    val node: URL,
    val conferenceAlias: String,
    val presentationInMain: Boolean,
    val response: RequestTokenResponse,
    val cameraVideoTrack: CameraVideoTrack?,
    val microphoneAudioTrack: LocalAudioTrack?,
)
