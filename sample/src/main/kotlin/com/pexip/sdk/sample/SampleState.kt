package com.pexip.sdk.sample

import com.pexip.sdk.api.infinity.RequestTokenResponse
import com.pexip.sdk.media.CameraVideoTrack
import com.pexip.sdk.media.LocalAudioTrack
import java.net.URL

data class SampleState(
    val destination: SampleDestination,
    val cameraVideoTrack: CameraVideoTrack? = null,
    val microphoneAudioTrack: LocalAudioTrack? = null,
    val cameraCapturing: Boolean? = null,
    val microphoneCapturing: Boolean? = null,
    val createCameraVideoTrackCount: UInt = 0u,
    val createMicrophoneAudioTrackCount: UInt = 0u,
)

sealed interface SampleDestination {

    object Permissions : SampleDestination

    object Preflight : SampleDestination

    data class Conference(
        val node: URL,
        val conferenceAlias: String,
        val presentationInMain: Boolean,
        val response: RequestTokenResponse,
    ) : SampleDestination
}
