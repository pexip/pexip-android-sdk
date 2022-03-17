package com.pexip.sdk.video.sample.conference

import com.pexip.sdk.video.Conference
import com.pexip.sdk.video.VideoTrack

data class ConferenceState(
    val conference: Conference,
    val localVideoTrack: VideoTrack? = null,
    val remoteVideoTrack: VideoTrack? = null,
)
