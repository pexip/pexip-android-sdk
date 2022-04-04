package com.pexip.sdk.video.sample.conference

import org.webrtc.EglBase
import org.webrtc.VideoTrack

data class ConferenceRendering(
    val sharedContext: EglBase.Context,
    val localVideoTrack: VideoTrack?,
    val remoteVideoTrack: VideoTrack?,
    val onBackClick: () -> Unit,
)
