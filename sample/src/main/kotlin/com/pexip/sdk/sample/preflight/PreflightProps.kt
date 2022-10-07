package com.pexip.sdk.sample.preflight

import com.pexip.sdk.media.CameraVideoTrack
import com.pexip.sdk.media.LocalAudioTrack

data class PreflightProps(
    val cameraVideoTrack: CameraVideoTrack?,
    val microphoneAudioTrack: LocalAudioTrack?,
)
