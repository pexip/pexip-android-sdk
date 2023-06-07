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

/**
 * A camera video track.
 */
public interface CameraVideoTrack : LocalVideoTrack {

    /**
     * Attempts to switch a camera to the next one in the list of cameras.
     *
     * @param callback a callback that will be invoked on either success or failure
     */
    public fun switchCamera(callback: SwitchCameraCallback)

    /**
     * Attempts to switch a camera to the specified one.
     *
     * @param deviceName a name of the camera to switch to
     * @param callback a callback that will be invoked on either success or failure
     */
    public fun switchCamera(deviceName: String, callback: SwitchCameraCallback)

    public interface Callback {

        /**
         * Invoked when the camera has been disconnected.
         */
        public fun onCameraDisconnected()
    }

    public interface SwitchCameraCallback {

        /**
         * Invoked when camera switch completed successfully.
         *
         * @param deviceName new camera name
         */
        public fun onSuccess(deviceName: String)

        /**
         * Invoked when camera switch completed successfully.
         *
         * @param front true if the newly switched camera is front-facing, false otherwise
         */
        @Deprecated(
            message = "Use onSuccess that contains deviceName as an argument.",
            level = DeprecationLevel.ERROR,
        )
        public fun onSuccess(front: Boolean)

        /**
         * Invoked when camera switch failed.
         *
         * @param error an error that occurred during the switch
         */
        public fun onFailure(error: String)
    }
}
