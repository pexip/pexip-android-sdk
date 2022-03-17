package com.pexip.sdk.video.sample.conference

import com.pexip.sdk.video.conference.VideoTrack
import com.squareup.workflow1.WorkflowAction

typealias ConferenceAction = WorkflowAction<ConferenceProps, ConferenceState, ConferenceOutput>

class OnBackClick : ConferenceAction() {

    override fun Updater.apply() {
        setOutput(ConferenceOutput.Back)
    }
}

class OnLocalVideoTrack(private val track: VideoTrack?) : ConferenceAction() {

    override fun Updater.apply() {
        state = state.copy(localVideoTrack = track)
    }
}

class OnRemoteVideoTrack(private val track: VideoTrack?) : ConferenceAction() {

    override fun Updater.apply() {
        state = state.copy(remoteVideoTrack = track)
    }
}
