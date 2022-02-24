package com.pexip.sdk.video.sample.conference

import com.pexip.sdk.video.Conference
import com.squareup.workflow1.WorkflowAction

typealias ConferenceAction = WorkflowAction<ConferenceProps, Conference, ConferenceOutput>

class OnBackClick : ConferenceAction() {

    override fun Updater.apply() {
        setOutput(ConferenceOutput.Back)
    }
}
