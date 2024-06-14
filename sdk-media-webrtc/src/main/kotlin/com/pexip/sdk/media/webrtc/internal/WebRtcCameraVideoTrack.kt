/*
 * Copyright 2022-2024 Pexip AS
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
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.webrtc.CameraVideoCapturer
import org.webrtc.EglBase
import org.webrtc.VideoSource
import org.webrtc.VideoTrack
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

internal class WebRtcCameraVideoTrack(
    private val factory: CameraVideoTrackFactory,
    applicationContext: Context,
    eglBase: EglBase?,
    @Volatile private var deviceName: String,
    private val videoCapturer: CameraVideoCapturer,
    videoSource: VideoSource,
    videoTrack: VideoTrack,
    context: CoroutineContext,
    signalingDispatcher: CoroutineDispatcher,
) : CameraVideoTrack, WebRtcLocalVideoTrack(
    applicationContext = applicationContext,
    eglBase = eglBase,
    videoCapturer = videoCapturer,
    videoSource = videoSource,
    videoTrack = videoTrack,
    context = context,
    signalingDispatcher = signalingDispatcher,
) {

    private val scope = CoroutineScope(context)

    override fun switchCamera(callback: CameraVideoTrack.SwitchCameraCallback) {
        scope.launch {
            val deviceNames = factory.getDeviceNames()
            if (deviceNames.size <= 1) {
                callback.safeOnFailure("No camera to switch to.")
                return@launch
            } else {
                val deviceNameIndex = deviceNames.indexOf(deviceName)
                val deviceName = deviceNames[(deviceNameIndex + 1) % deviceNames.size]
                switchCameraInternal(deviceName, callback)
            }
        }
    }

    override fun switchCamera(deviceName: String, callback: CameraVideoTrack.SwitchCameraCallback) {
        scope.launch { switchCameraInternal(deviceName, callback) }
    }

    private suspend fun switchCameraInternal(
        deviceName: String,
        callback: CameraVideoTrack.SwitchCameraCallback,
    ) = try {
        this.deviceName = videoCapturer.switchCamera(deviceName)
        withContext(signalingDispatcher) { callback.safeOnSuccess(deviceName) }
    } catch (e: SwitchCameraException) {
        withContext(signalingDispatcher) { callback.safeOnFailure(e.error) }
    }

    private suspend fun CameraVideoCapturer.switchCamera(deviceName: String) = suspendCoroutine {
        val handler = object : CameraVideoCapturer.CameraSwitchHandler {

            override fun onCameraSwitchDone(front: Boolean) {
                it.resume(deviceName)
            }

            override fun onCameraSwitchError(error: String) {
                it.resumeWithException(SwitchCameraException(error))
            }
        }
        switchCamera(handler, deviceName)
    }

    private class SwitchCameraException(val error: String) : RuntimeException(error)
}
