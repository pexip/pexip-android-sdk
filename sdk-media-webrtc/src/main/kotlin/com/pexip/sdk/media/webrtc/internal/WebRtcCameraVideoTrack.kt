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

import android.content.Context
import com.pexip.sdk.media.CameraVideoTrack
import com.pexip.sdk.media.CameraVideoTrackFactory
import org.webrtc.CameraVideoCapturer
import org.webrtc.EglBase
import org.webrtc.VideoSource
import org.webrtc.VideoTrack
import java.util.concurrent.Executor

internal class WebRtcCameraVideoTrack(
    private val factory: CameraVideoTrackFactory,
    applicationContext: Context,
    eglBase: EglBase?,
    @Volatile private var deviceName: String,
    private val videoCapturer: CameraVideoCapturer,
    videoSource: VideoSource,
    videoTrack: VideoTrack,
    workerExecutor: Executor,
    signalingExecutor: Executor,
) : CameraVideoTrack, WebRtcLocalVideoTrack(
    applicationContext = applicationContext,
    eglBase = eglBase,
    videoCapturer = videoCapturer,
    videoSource = videoSource,
    videoTrack = videoTrack,
    workerExecutor = workerExecutor,
    signalingExecutor = signalingExecutor,
) {

    override fun switchCamera(callback: CameraVideoTrack.SwitchCameraCallback) {
        workerExecutor.maybeExecute {
            val deviceNames = factory.getDeviceNames()
            if (deviceNames.size <= 1) {
                callback.safeOnFailure("No camera to switch to.")
                return@maybeExecute
            } else {
                val deviceNameIndex = deviceNames.indexOf(deviceName)
                val deviceName = deviceNames[(deviceNameIndex + 1) % deviceNames.size]
                switchCamera(deviceName, callback)
            }
        }
    }

    override fun switchCamera(deviceName: String, callback: CameraVideoTrack.SwitchCameraCallback) {
        workerExecutor.maybeExecute {
            val handler = object : CameraVideoCapturer.CameraSwitchHandler {

                override fun onCameraSwitchDone(front: Boolean) {
                    this@WebRtcCameraVideoTrack.deviceName = deviceName
                    signalingExecutor.maybeExecute {
                        callback.safeOnSuccess(deviceName)
                    }
                }

                override fun onCameraSwitchError(error: String) {
                    signalingExecutor.maybeExecute {
                        callback.safeOnFailure(error)
                    }
                }
            }
            videoCapturer.switchCamera(handler, deviceName)
        }
    }
}
