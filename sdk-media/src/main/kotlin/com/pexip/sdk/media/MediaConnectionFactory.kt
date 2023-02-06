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
 * Main entry point to the [MediaConnection] API for clients.
 *
 * Note: use [LocalAudioTrackFactory] and [CameraVideoTrackFactory] directly as
 * [MediaConnectionFactory] won't eventually implement these interfaces.
 */
public interface MediaConnectionFactory : LocalAudioTrackFactory, CameraVideoTrackFactory {

    /**
     * Creates a [MediaConnection] with the specified [config].
     *
     * @return a [MediaConnection]
     * @throws IllegalStateException if [MediaConnectionFactory] has been disposed
     */
    public fun createMediaConnection(config: MediaConnectionConfig): MediaConnection

    /**
     * Disposes this [MediaConnectionFactory] and releases any held resources.
     *
     * The instance will become unusable after this call.
     *
     * @throws IllegalStateException if [MediaConnectionFactory] has been disposed
     */
    public fun dispose()
}
