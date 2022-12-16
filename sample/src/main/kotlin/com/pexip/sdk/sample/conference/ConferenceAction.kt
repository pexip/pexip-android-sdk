package com.pexip.sdk.sample.conference

import android.content.Intent
import com.pexip.sdk.conference.ConferenceEvent
import com.pexip.sdk.conference.DisconnectConferenceEvent
import com.pexip.sdk.conference.FailureConferenceEvent
import com.pexip.sdk.conference.PresentationStartConferenceEvent
import com.pexip.sdk.conference.PresentationStopConferenceEvent
import com.pexip.sdk.media.LocalVideoTrack
import com.pexip.sdk.media.QualityProfile
import com.pexip.sdk.media.VideoTrack
import com.pexip.sdk.sample.audio.AudioDeviceOutput
import com.pexip.sdk.sample.composer.ComposerOutput
import com.pexip.sdk.sample.dtmf.DtmfOutput
import com.squareup.workflow1.WorkflowAction

typealias ConferenceAction = WorkflowAction<ConferenceProps, ConferenceState, ConferenceOutput>

class OnScreenCapture(private val data: Intent) : ConferenceAction() {

    override fun Updater.apply() {
        state = state.copy(screenCaptureData = data)
    }
}

class OnScreenCaptureVideoTrack(private val localVideoTrack: LocalVideoTrack) : ConferenceAction() {

    override fun Updater.apply() {
        state = state.copy(
            screenCaptureData = null,
            screenCaptureVideoTrack = localVideoTrack
        )
        state.connection.setPresentationVideoTrack(localVideoTrack)
        localVideoTrack.startCapture(QualityProfile.High)
    }
}

class OnStopScreenCapture : ConferenceAction() {

    override fun Updater.apply() {
        state.connection.setPresentationVideoTrack(null)
        state.screenCaptureVideoTrack?.dispose()
        state = state.copy(
            screenCapturing = false,
            screenCaptureVideoTrack = null
        )
    }
}

class OnAudioDevicesChange(private val visible: Boolean) : ConferenceAction() {

    override fun Updater.apply() {
        state = state.copy(audioDevicesVisible = visible)
    }
}

class OnDtmfChange(private val visible: Boolean) : ConferenceAction() {

    override fun Updater.apply() {
        state = state.copy(dtmfVisible = visible)
    }
}

class OnDtmfOutput(private val output: DtmfOutput) : ConferenceAction() {

    override fun Updater.apply() {
        when (output) {
            is DtmfOutput.Tone -> state.connection.dtmf(output.tone)
            is DtmfOutput.Back -> state = state.copy(dtmfVisible = false)
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

class OnScreenCapturing(private val capturing: Boolean) : ConferenceAction() {

    override fun Updater.apply() {
        state = state.copy(screenCapturing = capturing)
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
        if (conferenceEvent is PresentationStartConferenceEvent) {
            state.connection.setPresentationVideoTrack(null)
            state.screenCaptureVideoTrack?.dispose()
            state.connection.setPresentationRemoteVideoTrackEnabled(true)
        } else if (conferenceEvent is PresentationStopConferenceEvent) {
            state.connection.setPresentationRemoteVideoTrackEnabled(false)
        }
        state = state.copy(
            presentation = presentation,
            conferenceEvents = conferenceEvents,
            screenCapturing = when (conferenceEvent) {
                is PresentationStartConferenceEvent -> false
                else -> state.screenCapturing
            },
            screenCaptureVideoTrack = when (conferenceEvent) {
                is PresentationStartConferenceEvent -> null
                else -> state.screenCaptureVideoTrack
            }
        )
        if (conferenceEvent is DisconnectConferenceEvent || conferenceEvent is FailureConferenceEvent) {
            setOutput(ConferenceOutput.Back)
        }
    }
}

class OnPresentationRemoteVideoTrack(private val videoTrack: VideoTrack?) : ConferenceAction() {

    override fun Updater.apply() {
        state = state.copy(presentationRemoteVideoTrack = videoTrack)
    }
}

class OnAudioDeviceOutput(private val output: AudioDeviceOutput) : ConferenceAction() {

    override fun Updater.apply() {
        when (output) {
            is AudioDeviceOutput.Back -> state = state.copy(audioDevicesVisible = false)
        }
    }
}

class OnComposerOutput(private val output: ComposerOutput) : ConferenceAction() {

    override fun Updater.apply() {
        when (output) {
            is ComposerOutput.Submit -> state.conference.message(output.message)
        }
    }
}
