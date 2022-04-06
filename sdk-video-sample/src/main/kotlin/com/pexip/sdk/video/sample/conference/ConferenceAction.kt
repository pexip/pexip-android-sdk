package com.pexip.sdk.video.sample.conference

import com.squareup.workflow1.WorkflowAction
import org.webrtc.VideoTrack

typealias ConferenceAction = WorkflowAction<ConferenceProps, ConferenceState, ConferenceOutput>

class OnToggleMainVideoCapturing : ConferenceAction() {

    override fun Updater.apply() = with(state) {
        if (mainCapturing) {
            connection.stopMainCapture()
        } else {
            connection.startMainCapture()
        }
    }
}

class OnBackClick : ConferenceAction() {

    override fun Updater.apply() {
        setOutput(ConferenceOutput.Back)
    }
}

class OnMainLocalVideoTrack(private val videoTrack: VideoTrack?) : ConferenceAction() {

    override fun Updater.apply() {
        state = state.copy(localVideoTrack = videoTrack)
    }
}

class OnMainRemoteVideoTrack(private val videoTrack: VideoTrack?) : ConferenceAction() {

    override fun Updater.apply() {
        state = state.copy(remoteVideoTrack = videoTrack)
    }
}

class OnMainCapturing(private val capturing: Boolean) : ConferenceAction() {

    override fun Updater.apply() {
        state = state.copy(mainCapturing = capturing)
    }
}
