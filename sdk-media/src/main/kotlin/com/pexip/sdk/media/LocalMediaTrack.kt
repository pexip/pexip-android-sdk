/*
 * Copyright 2022 Pexip AS
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
 * A local media track.
 *
 * @property capturing true if capturing, false otherwise
 */
public interface LocalMediaTrack {

    public val capturing: Boolean

    /**
     * Start the capture.
     *
     * Implementations should use [QualityProfile.Medium] if they support changing profiles
     */
    public fun startCapture()

    /**
     * Stops the capture.
     */
    public fun stopCapture()

    /**
     * Registers a [CapturingListener] that will be notified when capturing state changes.
     *
     * @param listener a listener to register
     */
    public fun registerCapturingListener(listener: CapturingListener)

    /**
     * Unregisters a previously registered [CapturingListener].
     *
     * @param listener a listener to unregister
     */
    public fun unregisterCapturingListener(listener: CapturingListener)

    /**
     * Disposes this [LocalMediaTrack] and frees any resources held by it.
     */
    public fun dispose()

    /**
     * A listener that notifies of capturing state changes.
     */
    public fun interface CapturingListener {

        /**
         * Invoked when the capturing state changes.
         *
         * @param capturing true if capturing, false otherwise
         */
        public fun onCapturing(capturing: Boolean)
    }
}
