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
        val handler = object : CameraVideoCapturer.CameraSwitchHandler {

            override fun onCameraSwitchDone(front: Boolean) {
                signalingExecutor.maybeExecute {
                    callback.safeOnSuccess(front)
                }
            }

            override fun onCameraSwitchError(error: String) {
                signalingExecutor.maybeExecute {
                    callback.safeOnFailure(error)
                }
            }
        }
        workerExecutor.maybeExecute {
            videoCapturer.switchCamera(handler)
        }
    }
}
