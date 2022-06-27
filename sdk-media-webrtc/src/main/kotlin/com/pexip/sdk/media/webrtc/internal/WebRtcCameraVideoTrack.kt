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
) : CameraVideoTrack, WebRtcLocalVideoTrack(
    applicationContext = applicationContext,
    textureHelper = textureHelper,
    videoCapturer = videoCapturer,
    videoSource = videoSource,
    videoTrack = videoTrack,
    workerExecutor = workerExecutor
) {

    override fun switchCamera(callback: CameraVideoTrack.SwitchCameraCallback) {
        val handler = object : CameraVideoCapturer.CameraSwitchHandler {

            override fun onCameraSwitchDone(front: Boolean) {
                callback.onSuccess(front)
            }

            override fun onCameraSwitchError(error: String) {
                callback.onFailure(error)
            }
        }
        videoCapturer.switchCamera(handler)
    }
}
