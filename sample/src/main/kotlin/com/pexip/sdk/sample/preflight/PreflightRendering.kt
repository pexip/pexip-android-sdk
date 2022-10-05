package com.pexip.sdk.sample.preflight

import com.pexip.sdk.media.VideoTrack
import com.pexip.sdk.sample.media.LocalMediaTrackRendering

data class PreflightRendering(
    val childRendering: Any?,
    val cameraVideoTrack: VideoTrack?,
    val onCallClick: () -> Unit,
    val cameraVideoTrackRendering: LocalMediaTrackRendering?,
    val microphoneAudioTrackRendering: LocalMediaTrackRendering?,
    val onBackClick: () -> Unit,
)
