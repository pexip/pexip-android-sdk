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
