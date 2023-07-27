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
import org.webrtc.RtpParameters
import org.webrtc.RtpParameters.Encoding
import org.webrtc.RtpTransceiver
import org.webrtc.RtpTransceiver.RtpTransceiverDirection
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
            RtpTransceiverDirection.SEND_ONLY -> RtpTransceiverDirection.INACTIVE
            RtpTransceiverDirection.SEND_RECV -> RtpTransceiverDirection.RECV_ONLY
            else -> null
        }
        else -> when (direction) {
            RtpTransceiverDirection.INACTIVE -> RtpTransceiverDirection.SEND_ONLY
            RtpTransceiverDirection.RECV_ONLY -> RtpTransceiverDirection.SEND_RECV
            else -> null
        }
    }
    newDirection?.let(::setDirection)
}

internal fun RtpTransceiver.maybeSetNewDirection(receive: Boolean) {
    val newDirection = when (receive) {
        true -> when (direction) {
            RtpTransceiverDirection.INACTIVE -> RtpTransceiverDirection.RECV_ONLY
            RtpTransceiverDirection.SEND_ONLY -> RtpTransceiverDirection.SEND_RECV
            else -> null
        }
        else -> when (direction) {
            RtpTransceiverDirection.RECV_ONLY -> RtpTransceiverDirection.INACTIVE
            RtpTransceiverDirection.SEND_RECV -> RtpTransceiverDirection.SEND_ONLY
            else -> null
        }
    }
    newDirection?.let(::setDirection)
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

internal fun RtpTransceiverInit(
    direction: RtpTransceiverDirection = RtpTransceiverDirection.SEND_RECV,
    streamIds: List<String> = emptyList(),
    sendEncodings: List<Encoding> = emptyList(),
) = RtpTransceiver.RtpTransceiverInit(direction, streamIds, sendEncodings)

internal fun Encoding(
    rid: String? = null,
    active: Boolean = true,
    scaleResolutionDownBy: Double? = null,
    block: Encoding.() -> Unit = { },
) = Encoding(rid, active, scaleResolutionDownBy).apply(block)

internal const val MAX_FRAMERATE = 30
