package com.pexip.sdk.sample.conference

import android.content.Intent
import com.pexip.sdk.conference.Conference
import com.pexip.sdk.conference.ConferenceEvent
import com.pexip.sdk.media.LocalVideoTrack
import com.pexip.sdk.media.MediaConnection
import com.pexip.sdk.media.VideoTrack

data class ConferenceState(
    val conference: Conference,
    val connection: MediaConnection,
    val screenCaptureData: Intent? = null,
    val screenCaptureVideoTrack: LocalVideoTrack? = null,
    val screenCapturing: Boolean = screenCaptureVideoTrack?.capturing ?: false,
    val mainRemoteVideoTrack: VideoTrack? = connection.mainRemoteVideoTrack,
    val presentation: Boolean = false,
    val presentationRemoteVideoTrack: VideoTrack? = connection.presentationRemoteVideoTrack,
    val audioDevicesVisible: Boolean = false,
    val dtmfVisible: Boolean = false,
    val showingConferenceEvents: Boolean = false,
    val conferenceEvents: List<ConferenceEvent> = emptyList(),
    val message: String = "",
)
