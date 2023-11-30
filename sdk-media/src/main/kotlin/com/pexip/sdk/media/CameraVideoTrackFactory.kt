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
package com.pexip.sdk.media

public interface CameraVideoTrackFactory {

    /**
     * Returns all available device names.
     *
     * @return a list of available device names
     */
    public fun getDeviceNames(): List<String>

    /**
     * Checks whether the provided device name belongs to a front-facing camera.
     *
     * @return true if the camera is front-facing, false otherwise
     */
    public fun isFrontFacing(deviceName: String): Boolean

    /**
     * Checks whether the provided device name belongs to a back-facing camera.
     *
     * @return true if the camera is back-facing, false otherwise
     */
    public fun isBackFacing(deviceName: String): Boolean

    /**
     * Returns a list of supported quality profiles for a specific camera.
     *
     * @return a list of supported quality profiles
     */
    public fun getQualityProfiles(deviceName: String): List<QualityProfile>

    /**
     * Creates a [CameraVideoTrack] for the best available camera.
     *
     * Best available camera is determined by the following order:
     * 1. First front-facing camera
     * 2. First back-facing camera
     * 3. First available camera
     *
     * @param callback a callback used to signal various camera events
     * @return a [CameraVideoTrack]
     * @throws IllegalStateException if no camera is available
     * @throws IllegalStateException if [CameraVideoTrackFactory] has been disposed
     */
    public fun createCameraVideoTrack(callback: CameraVideoTrack.Callback): CameraVideoTrack

    /**
     * Creates a [CameraVideoTrack] for specific [deviceName].
     *
     * @param deviceName a device name that should be opened
     * @param callback a callback used to signal various camera events
     * @return a [CameraVideoTrack]
     * @throws IllegalStateException if [deviceName] is not available
     * @throws IllegalStateException if [CameraVideoTrackFactory] has been disposed
     */
    public fun createCameraVideoTrack(
        deviceName: String,
        callback: CameraVideoTrack.Callback,
    ): CameraVideoTrack
}
