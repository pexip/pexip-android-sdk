package com.pexip.sdk.video.sample.conference

import com.pexip.sdk.conference.ConferenceEvent
import com.pexip.sdk.conference.PresentationStartConferenceEvent
import com.pexip.sdk.conference.PresentationStopConferenceEvent
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

class OnConferenceEventsClick : ConferenceAction() {

    override fun Updater.apply() {
        state = state.copy(showingConferenceEvents = true)
    }
}

class OnBackClick : ConferenceAction() {

    override fun Updater.apply() {
        if (state.showingConferenceEvents) {
            state = state.copy(showingConferenceEvents = false)
        } else {
            setOutput(ConferenceOutput.Back)
        }
    }
}

class OnMainLocalVideoTrack(private val videoTrack: VideoTrack?) : ConferenceAction() {

    override fun Updater.apply() {
        state = state.copy(mainLocalVideoTrack = videoTrack)
    }
}

class OnMainRemoteVideoTrack(private val videoTrack: VideoTrack?) : ConferenceAction() {

    override fun Updater.apply() {
        state = state.copy(mainRemoteVideoTrack = videoTrack)
    }
}

class OnMainCapturing(private val capturing: Boolean) : ConferenceAction() {

    override fun Updater.apply() {
        state = state.copy(mainCapturing = capturing)
    }
}

class OnConferenceEvent(private val conferenceEvent: ConferenceEvent) : ConferenceAction() {

    override fun Updater.apply() {
        val presentation = when (conferenceEvent) {
            is PresentationStartConferenceEvent -> true
            is PresentationStopConferenceEvent -> false
            else -> state.presentation
        }
        val conferenceEvents = state.conferenceEvents.asSequence()
            .plus(conferenceEvent)
            .sortedBy { it.at }
            .toList()
        state = state.copy(
            presentation = presentation,
            conferenceEvents = conferenceEvents
        )
    }
}

class OnMessageChange(private val message: String) : ConferenceAction() {

    override fun Updater.apply() {
        state = state.copy(message = message)
    }
}

class OnSubmitClick : ConferenceAction() {

    override fun Updater.apply() {
        val message = state.message.takeIf { it.isNotBlank() }?.trim() ?: return
        state.conference.message(message)
        state = state.copy(message = "")
    }
}

class OnPresentationRemoteVideoTrack(private val videoTrack: VideoTrack?) : ConferenceAction() {

    override fun Updater.apply() {
        state = state.copy(presentationRemoteVideoTrack = videoTrack)
    }
}
