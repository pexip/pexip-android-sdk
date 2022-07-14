package com.pexip.sdk.media.webrtc.internal

import com.pexip.sdk.media.CameraVideoTrack
import org.webrtc.CameraVideoCapturer
import java.util.concurrent.Executor

internal class SimpleCameraEventsHandler(
    private val callback: CameraVideoTrack.Callback,
    private val signalingExecutor: Executor,
) : CameraVideoCapturer.CameraEventsHandler {

    override fun onCameraDisconnected() {
        signalingExecutor.maybeExecute {
            callback.onCameraDisconnected()
        }
    }

    override fun onCameraError(reason: String) {
        signalingExecutor.maybeExecute {
            callback.onCameraDisconnected()
        }
    }

    override fun onCameraFreezed(reason: String) {
    }

    override fun onCameraOpening(deviceName: String) {
    }

    override fun onFirstFrameAvailable() {
    }

    override fun onCameraClosed() {
    }
}
