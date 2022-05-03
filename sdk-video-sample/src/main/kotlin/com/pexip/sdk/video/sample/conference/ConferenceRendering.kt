package com.pexip.sdk.video.sample.conference

import com.pexip.sdk.conference.ConferenceEvent
import com.pexip.sdk.media.VideoTrack
import org.webrtc.EglBase

sealed interface ConferenceRendering {
    val onBackClick: () -> Unit
}

data class ConferenceCallRendering(
    val sharedContext: EglBase.Context,
    val mainCapturing: Boolean,
    val mainLocalVideoTrack: VideoTrack,
    val mainRemoteVideoTrack: VideoTrack?,
    val presentationRemoteVideoTrack: VideoTrack?,
    val onToggleMainCapturing: () -> Unit,
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
