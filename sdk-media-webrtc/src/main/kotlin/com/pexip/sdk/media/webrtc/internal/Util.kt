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
package com.pexip.sdk.media.webrtc.internal

import com.pexip.sdk.media.CameraVideoTrack
import com.pexip.sdk.media.DegradationPreference
import com.pexip.sdk.media.LocalMediaTrack
import com.pexip.sdk.media.MediaConnection
import com.pexip.sdk.media.QualityProfile
import com.pexip.sdk.media.VideoTrack
import org.webrtc.CameraEnumerationAndroid.CaptureFormat
import org.webrtc.MediaStreamTrack
import org.webrtc.PeerConnection
import org.webrtc.RtpParameters
import org.webrtc.RtpTransceiver
import java.util.concurrent.Executor
import java.util.concurrent.RejectedExecutionException

internal fun Executor.maybeExecute(block: () -> Unit) = try {
    execute(block)
} catch (e: RejectedExecutionException) {
    // noop
}

internal fun LocalMediaTrack.CapturingListener.safeOnCapturing(capturing: Boolean) = try {
    onCapturing(capturing)
} catch (t: Throwable) {
    // noop
}

internal fun CameraVideoTrack.Callback.safeOnCameraDisconnected() = try {
    onCameraDisconnected()
} catch (t: Throwable) {
    // noop
}

internal fun CameraVideoTrack.SwitchCameraCallback.safeOnSuccess(deviceName: String) = try {
    onSuccess(deviceName)
} catch (t: Throwable) {
    // noop
}

internal fun CameraVideoTrack.SwitchCameraCallback.safeOnFailure(error: String) = try {
    onFailure(error)
} catch (t: Throwable) {
    // noop
}

internal fun MediaConnection.RemoteVideoTrackListener.safeOnRemoteVideoTrack(videoTrack: VideoTrack?) =
    try {
        onRemoteVideoTrack(videoTrack)
    } catch (t: Throwable) {
        // noop
    }

internal fun QualityProfile.Companion.from(format: CaptureFormat) = QualityProfile(
    width = format.width,
    height = format.height,
    fps = format.framerate.max / 1000,
)

internal fun RtpTransceiver.setDegradationPreference(preference: DegradationPreference) {
    val parameters = sender.parameters
    parameters.degradationPreference = when (preference) {
        DegradationPreference.BALANCED -> RtpParameters.DegradationPreference.BALANCED
        DegradationPreference.MAINTAIN_FRAMERATE -> RtpParameters.DegradationPreference.MAINTAIN_FRAMERATE
        DegradationPreference.MAINTAIN_RESOLUTION -> RtpParameters.DegradationPreference.MAINTAIN_RESOLUTION
        DegradationPreference.DISABLED -> RtpParameters.DegradationPreference.DISABLED
    }
    sender.parameters = parameters
}

internal fun RtpTransceiver.maybeSetNewDirection(track: LocalMediaTrack?) {
    val newDirection = when (track) {
        null -> when (direction) {
            RtpTransceiver.RtpTransceiverDirection.SEND_ONLY -> RtpTransceiver.RtpTransceiverDirection.INACTIVE
            RtpTransceiver.RtpTransceiverDirection.SEND_RECV -> RtpTransceiver.RtpTransceiverDirection.RECV_ONLY
            else -> null
        }
        else -> when (direction) {
            RtpTransceiver.RtpTransceiverDirection.INACTIVE -> RtpTransceiver.RtpTransceiverDirection.SEND_ONLY
            RtpTransceiver.RtpTransceiverDirection.RECV_ONLY -> RtpTransceiver.RtpTransceiverDirection.SEND_RECV
            else -> null
        }
    }
    newDirection?.let(::setDirection)
}

internal fun RtpTransceiver.maybeSetNewDirection(receive: Boolean) {
    val newDirection = when (receive) {
        true -> when (direction) {
            RtpTransceiver.RtpTransceiverDirection.INACTIVE -> RtpTransceiver.RtpTransceiverDirection.RECV_ONLY
            RtpTransceiver.RtpTransceiverDirection.SEND_ONLY -> RtpTransceiver.RtpTransceiverDirection.SEND_RECV
            else -> null
        }
        else -> when (direction) {
            RtpTransceiver.RtpTransceiverDirection.RECV_ONLY -> RtpTransceiver.RtpTransceiverDirection.INACTIVE
            RtpTransceiver.RtpTransceiverDirection.SEND_RECV -> RtpTransceiver.RtpTransceiverDirection.SEND_ONLY
            else -> null
        }
    }
    newDirection?.let(::setDirection)
}

internal fun PeerConnection.maybeAddTransceiver(track: LocalMediaTrack?): RtpTransceiver? {
    track ?: return null
    val mediaType = when (track) {
        is WebRtcLocalVideoTrack -> MediaStreamTrack.MediaType.MEDIA_TYPE_VIDEO
        is WebRtcLocalAudioTrack -> MediaStreamTrack.MediaType.MEDIA_TYPE_AUDIO
        else -> throw IllegalArgumentException("Unknown LocalMediaTrack: $track.")
    }
    val init = RtpTransceiver.RtpTransceiverInit(RtpTransceiver.RtpTransceiverDirection.SEND_ONLY)
    return addTransceiver(mediaType, init)
}

internal fun PeerConnection.maybeAddTransceiver(
    mediaType: MediaStreamTrack.MediaType,
    receive: Boolean,
): RtpTransceiver? {
    if (!receive) return null
    val init = RtpTransceiver.RtpTransceiverInit(RtpTransceiver.RtpTransceiverDirection.RECV_ONLY)
    return addTransceiver(mediaType, init)
}

internal fun RtpTransceiver.setTrack(track: LocalMediaTrack?) {
    val t = when (track) {
        is WebRtcLocalVideoTrack -> track.videoTrack
        is WebRtcLocalAudioTrack -> track.audioTrack
        null -> null
        else -> throw IllegalArgumentException("Unknown LocalMediaTrack: $track.")
    }
    sender.setTrack(t, false)
}
