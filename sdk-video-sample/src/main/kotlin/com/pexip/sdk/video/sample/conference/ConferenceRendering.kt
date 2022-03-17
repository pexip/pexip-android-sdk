package com.pexip.sdk.video.sample.conference

import com.pexip.sdk.video.VideoTrack

data class ConferenceRendering(
    val localVideoTrack: VideoTrack?,
    val remoteVideoTrack: VideoTrack?,
    val onBackClick: () -> Unit,
)
