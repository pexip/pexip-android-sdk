package com.pexip.sdk.media.webrtc.internal

import android.content.Context
import com.pexip.sdk.media.CameraVideoTrack
import org.webrtc.CameraVideoCapturer
import org.webrtc.SurfaceTextureHelper
import org.webrtc.VideoSource
import org.webrtc.VideoTrack
import java.util.concurrent.Executor

internal class WebRtcCameraVideoTrack(
    applicationContext: Context,
    textureHelper: SurfaceTextureHelper,
    private val videoCapturer: CameraVideoCapturer,
    videoSource: VideoSource,
    videoTrack: VideoTrack,
    workerExecutor: Executor,
    signalingExecutor: Executor,
    private val checkDeviceName: (String) -> Unit,
) : CameraVideoTrack, WebRtcLocalVideoTrack(
    applicationContext = applicationContext,
    textureHelper = textureHelper,
    videoCapturer = videoCapturer,
    videoSource = videoSource,
    videoTrack = videoTrack,
    workerExecutor = workerExecutor,
    signalingExecutor = signalingExecutor
) {

    override fun switchCamera(callback: CameraVideoTrack.SwitchCameraCallback) {
        workerExecutor.maybeExecute {
            videoCapturer.switchCamera(callback.toCameraSwitchHandler())
        }
    }

    override fun switchCamera(deviceName: String, callback: CameraVideoTrack.SwitchCameraCallback) {
        checkDeviceName(deviceName)
        workerExecutor.maybeExecute {
            videoCapturer.switchCamera(callback.toCameraSwitchHandler(), deviceName)
        }
    }

    private fun CameraVideoTrack.SwitchCameraCallback.toCameraSwitchHandler() =
        object : CameraVideoCapturer.CameraSwitchHandler {

            override fun onCameraSwitchDone(front: Boolean) {
                signalingExecutor.maybeExecute {
                    safeOnSuccess(front)
                }
            }

            override fun onCameraSwitchError(error: String) {
                signalingExecutor.maybeExecute {
                    safeOnFailure(error)
                }
            }
        }
}
