/*
 * Copyright 2022-2023 Pexip AS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.pexip.sdk.sample.conference

import android.content.Intent
import com.pexip.sdk.conference.Conference
import com.pexip.sdk.conference.DisconnectConferenceEvent
import com.pexip.sdk.conference.FailureConferenceEvent
import com.pexip.sdk.conference.Message
import com.pexip.sdk.conference.PresentationStartConferenceEvent
import com.pexip.sdk.conference.PresentationStopConferenceEvent
import com.pexip.sdk.media.LocalVideoTrack
import com.pexip.sdk.media.QualityProfile
import com.pexip.sdk.media.VideoTrack
import com.pexip.sdk.sample.audio.AudioDeviceOutput
import com.pexip.sdk.sample.bandwidth.BandwidthOutput
import com.pexip.sdk.sample.bandwidth.bitrate
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
            screenCaptureVideoTrack = localVideoTrack,
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
            screenCaptureVideoTrack = null,
        )
    }
}

class OnAudioDevicesChange(private val visible: Boolean) : ConferenceAction() {

    override fun Updater.apply() {
        state = state.copy(audioDevicesVisible = visible)
    }
}

class OnBandwidthChange(private val visible: Boolean) : ConferenceAction() {

    override fun Updater.apply() {
        state = state.copy(bandwidthVisible = visible)
    }
}

class OnBandwidthOutput(private val output: BandwidthOutput) : ConferenceAction() {

    override fun Updater.apply() {
        if (output is BandwidthOutput.ChangeBandwidth) {
            state.connection.setMaxBitrate(output.bandwidth.bitrate)
        }
        state = state.copy(bandwidthVisible = false)
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

class OnChatClick : ConferenceAction() {

    override fun Updater.apply() {
        state = state.copy(showingChat = true)
    }
}

class OnBackClick : ConferenceAction() {

    override fun Updater.apply() {
        if (state.showingChat) {
            state = state.copy(showingChat = false)
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

class OnMessage(private val message: Message) : ConferenceAction() {

    override fun Updater.apply() {
        state = state.copy(messages = state.messages + message)
    }
}

@Suppress("unused")
class OnPresentationStartConferenceEvent(private val event: PresentationStartConferenceEvent) :
    ConferenceAction() {

    override fun Updater.apply() {
        state.connection.setPresentationVideoTrack(null)
        state.screenCaptureVideoTrack?.dispose()
        state.connection.setPresentationRemoteVideoTrackEnabled(true)
        state = state.copy(
            presentation = true,
            screenCapturing = false,
            screenCaptureVideoTrack = null,
        )
    }
}

@Suppress("unused")
class OnPresentationStopConferenceEvent(private val event: PresentationStopConferenceEvent) :
    ConferenceAction() {

    override fun Updater.apply() {
        state.connection.setPresentationRemoteVideoTrackEnabled(false)
        state = state.copy(presentation = false)
    }
}

class OnReferConferenceEvent(private val conference: Conference) : ConferenceAction() {

    override fun Updater.apply() {
        setOutput(ConferenceOutput.Refer(conference))
    }
}

@Suppress("unused")
class OnDisconnectConferenceEvent(private val event: DisconnectConferenceEvent) :
    ConferenceAction() {

    override fun Updater.apply() {
        setOutput(ConferenceOutput.Back)
    }
}

@Suppress("unused")
class OnFailureConferenceEvent(private val event: FailureConferenceEvent) : ConferenceAction() {

    override fun Updater.apply() {
        setOutput(ConferenceOutput.Back)
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
            is ComposerOutput.Submit -> state.message.tryEmit(output.message)
        }
    }
}
