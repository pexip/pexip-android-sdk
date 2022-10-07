package com.pexip.sdk.sample.preflight

import com.pexip.sdk.media.VideoTrack
import com.pexip.sdk.sample.media.LocalMediaTrackRendering

data class PreflightRendering(
    val childRendering: Any?,
    val cameraVideoTrack: VideoTrack?,
    val callEnabled: Boolean,
    val onCallClick: () -> Unit,
    val onCreateCameraVideoTrackClick: () -> Unit,
    val cameraVideoTrackRendering: LocalMediaTrackRendering?,
    val microphoneAudioTrackRendering: LocalMediaTrackRendering?,
    val onBackClick: () -> Unit,
)
