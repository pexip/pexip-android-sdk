package com.pexip.sdk.video.sample.conference

import com.pexip.sdk.media.webrtc.WebRtcMediaConnection
import com.pexip.sdk.video.conference.Conference
import org.webrtc.EglBase
import org.webrtc.VideoTrack

data class ConferenceState(
    val conference: Conference,
    val connection: WebRtcMediaConnection,
    val sharedContext: EglBase.Context,
    val localVideoTrack: VideoTrack? = null,
    val remoteVideoTrack: VideoTrack? = null,
)
