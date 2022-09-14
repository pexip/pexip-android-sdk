package com.pexip.sdk.media.webrtc.internal

import com.pexip.sdk.media.CameraVideoTrack
import com.pexip.sdk.media.LocalMediaTrack
import com.pexip.sdk.media.MediaConnection
import com.pexip.sdk.media.QualityProfile
import com.pexip.sdk.media.VideoTrack
import org.webrtc.CameraEnumerationAndroid.CaptureFormat
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

@Deprecated("Use safeOnSuccess that contains deviceName as an argument.")
internal fun CameraVideoTrack.SwitchCameraCallback.safeOnSuccess(front: Boolean) = try {
    onSuccess(front)
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
    fps = format.framerate.max / 1000
)
