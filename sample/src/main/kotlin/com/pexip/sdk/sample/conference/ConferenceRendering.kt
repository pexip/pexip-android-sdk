package com.pexip.sdk.sample.conference

import android.content.Intent
import com.pexip.sdk.conference.ConferenceEvent
import com.pexip.sdk.media.VideoTrack
import com.pexip.sdk.sample.audio.AudioDeviceRendering
import com.pexip.sdk.sample.composer.ComposerRendering
import com.pexip.sdk.sample.dtmf.DtmfRendering
import com.pexip.sdk.sample.media.LocalMediaTrackRendering

sealed interface ConferenceRendering {
    val onBackClick: () -> Unit
}

data class ConferenceCallRendering(
    val cameraVideoTrack: VideoTrack?,
    val mainRemoteVideoTrack: VideoTrack?,
    val presentationRemoteVideoTrack: VideoTrack?,
    val audioDeviceRendering: AudioDeviceRendering,
    val dtmfRendering: DtmfRendering,
    val cameraVideoTrackRendering: LocalMediaTrackRendering?,
    val microphoneAudioTrackRendering: LocalMediaTrackRendering?,
    val screenCapturing: Boolean,
    val onScreenCapture: (Intent) -> Unit,
    val onAudioDevicesChange: (Boolean) -> Unit,
    val onDtmfChange: (Boolean) -> Unit,
    val onStopScreenCapture: () -> Unit,
    val onConferenceEventsClick: () -> Unit,
    override val onBackClick: () -> Unit,
) : ConferenceRendering

data class ConferenceEventsRendering(
    val conferenceEvents: List<ConferenceEvent>,
    val composerRendering: ComposerRendering,
    override val onBackClick: () -> Unit,
) : ConferenceRendering
