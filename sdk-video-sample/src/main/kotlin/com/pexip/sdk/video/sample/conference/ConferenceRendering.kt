package com.pexip.sdk.video.sample.conference

import org.webrtc.EglBase
import org.webrtc.VideoTrack

data class ConferenceRendering(
    val sharedContext: EglBase.Context,
    val mainCapturing: Boolean,
    val mainLocalVideoTrack: VideoTrack?,
    val mainRemoteVideoTrack: VideoTrack?,
    val presentationRemoteVideoTrack: VideoTrack?,
    val onToggleMainCapturing: () -> Unit,
    val onBackClick: () -> Unit,
)
