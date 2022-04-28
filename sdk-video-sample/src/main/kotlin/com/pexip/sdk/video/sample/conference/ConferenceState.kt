package com.pexip.sdk.video.sample.conference

import com.pexip.sdk.conference.Conference
import com.pexip.sdk.conference.ConferenceEvent
import com.pexip.sdk.media.LocalAudioTrack
import com.pexip.sdk.media.webrtc.WebRtcMediaConnection
import org.webrtc.EglBase
import org.webrtc.VideoTrack

data class ConferenceState(
    val conference: Conference,
    val connection: WebRtcMediaConnection,
    val sharedContext: EglBase.Context,
    val localAudioTrack: LocalAudioTrack,
    val mainCapturing: Boolean = false,
    val mainLocalVideoTrack: VideoTrack? = null,
    val mainRemoteVideoTrack: VideoTrack? = null,
    val presentation: Boolean = false,
    val presentationRemoteVideoTrack: VideoTrack? = null,
    val showingConferenceEvents: Boolean = false,
    val conferenceEvents: List<ConferenceEvent> = emptyList(),
    val message: String = "",
) {

    val submitEnabled: Boolean
        get() = message.isNotBlank()
}
