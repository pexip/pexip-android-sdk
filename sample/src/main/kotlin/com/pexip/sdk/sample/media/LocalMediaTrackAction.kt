package com.pexip.sdk.sample.media

import com.squareup.workflow1.WorkflowAction

typealias LocalMediaTrackAction = WorkflowAction<LocalMediaTrackProps, LocalMediaTrackState, Nothing>

class OnCapturingChange(private val capturing: Boolean) : LocalMediaTrackAction() {

    override fun Updater.apply() {
        val track = props.localMediaTrack
        when (capturing) {
            true -> track.startCapture()
            else -> track.stopCapture()
        }
    }
}

class OnCapturingStateChange(private val capturing: Boolean) : LocalMediaTrackAction() {

    override fun Updater.apply() {
        state = LocalMediaTrackState(capturing)
    }
}
