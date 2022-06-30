package com.pexip.sdk.sample.conference

import android.content.Intent
import com.pexip.sdk.conference.Conference
import com.pexip.sdk.conference.ConferenceEvent
import com.pexip.sdk.media.CameraVideoTrack
import com.pexip.sdk.media.LocalAudioTrack
import com.pexip.sdk.media.LocalVideoTrack
import com.pexip.sdk.media.MediaConnection
import com.pexip.sdk.media.VideoTrack

data class ConferenceState(
    val conference: Conference,
    val connection: MediaConnection,
    val localAudioTrack: LocalAudioTrack,
    val cameraVideoTrack: CameraVideoTrack,
    val screenCaptureData: Intent? = null,
    val screenCaptureVideoTrack: LocalVideoTrack? = null,
    val localAudioCapturing: Boolean = false,
    val cameraCapturing: Boolean = false,
    val screenCapturing: Boolean = false,
    val mainRemoteVideoTrack: VideoTrack? = null,
    val presentation: Boolean = false,
    val presentationRemoteVideoTrack: VideoTrack? = null,
    val showingDtmf: Boolean = false,
    val showingConferenceEvents: Boolean = false,
    val conferenceEvents: List<ConferenceEvent> = emptyList(),
    val message: String = "",
) {

    val submitEnabled: Boolean
        get() = message.isNotBlank()
}
