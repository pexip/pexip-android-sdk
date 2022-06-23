package com.pexip.sdk.sample.conference

import com.pexip.sdk.conference.ConferenceEvent
import com.pexip.sdk.conference.PresentationStartConferenceEvent
import com.pexip.sdk.conference.PresentationStopConferenceEvent
import com.pexip.sdk.media.QualityProfile
import com.pexip.sdk.media.VideoTrack
import com.pexip.sdk.sample.dtmf.DtmfOutput
import com.squareup.workflow1.WorkflowAction

typealias ConferenceAction = WorkflowAction<ConferenceProps, ConferenceState, ConferenceOutput>

class OnToggleDtmf : ConferenceAction() {

    override fun Updater.apply() {
        state = state.copy(showingDtmf = !state.showingDtmf)
    }
}

class OnDtmfOutput(private val output: DtmfOutput) : ConferenceAction() {

    override fun Updater.apply() {
        when (output) {
            DtmfOutput.Back -> state = state.copy(showingDtmf = false)
        }
    }
}

class OnToggleBlur : ConferenceAction() {

    override fun Updater.apply() = with(state) {
        cameraVideoTrack.toggleBlur()
    }
}

class OnToggleLocalAudioCapturing : ConferenceAction() {

    override fun Updater.apply() = with(state) {
        if (localAudioCapturing) {
            localAudioTrack.stopCapture()
        } else {
            localAudioTrack.startCapture()
        }
    }
}

class OnToggleCameraCapturing : ConferenceAction() {

    override fun Updater.apply() = with(state) {
        if (cameraCapturing) {
            cameraVideoTrack.stopCapture()
        } else {
            cameraVideoTrack.startCapture(QualityProfile.High)
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

class OnMainRemoteVideoTrack(private val videoTrack: VideoTrack?) : ConferenceAction() {

    override fun Updater.apply() {
        state = state.copy(mainRemoteVideoTrack = videoTrack)
    }
}

class OnMicrophoneCapturing(private val capturing: Boolean) : ConferenceAction() {

    override fun Updater.apply() {
        state = state.copy(localAudioCapturing = capturing)
    }
}

class OnCameraCapturing(private val capturing: Boolean) : ConferenceAction() {

    override fun Updater.apply() {
        state = state.copy(cameraCapturing = capturing)
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
