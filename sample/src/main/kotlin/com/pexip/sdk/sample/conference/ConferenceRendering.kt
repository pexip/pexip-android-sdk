package com.pexip.sdk.sample.conference

import com.pexip.sdk.conference.ConferenceEvent
import com.pexip.sdk.media.VideoTrack
import com.pexip.sdk.sample.dtmf.DtmfRendering

sealed interface ConferenceRendering {
    val onBackClick: () -> Unit
}

data class ConferenceCallRendering(
    val localAudioCapturing: Boolean,
    val cameraCapturing: Boolean,
    val cameraVideoTrack: VideoTrack,
    val mainRemoteVideoTrack: VideoTrack?,
    val presentationRemoteVideoTrack: VideoTrack?,
    val dtmfRendering: DtmfRendering?,
    val onToggleDtmfClick: () -> Unit,
    val onToggleLocalAudioCapturing: () -> Unit,
    val onToggleCameraCapturing: () -> Unit,
    val onConferenceEventsClick: () -> Unit,
    override val onBackClick: () -> Unit,
) : ConferenceRendering

data class ConferenceEventsRendering(
    val conferenceEvents: List<ConferenceEvent>,
    val message: String,
    val onMessageChange: (String) -> Unit,
    val submitEnabled: Boolean,
    val onSubmitClick: () -> Unit,
    override val onBackClick: () -> Unit,
) : ConferenceRendering
