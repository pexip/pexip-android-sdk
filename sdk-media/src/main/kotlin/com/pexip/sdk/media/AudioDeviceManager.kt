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
package com.pexip.sdk.media

/**
 * An audio device manager.
 *
 * @sample com.pexip.sdk.media.AudioDeviceManagerSample
 *
 * @property availableAudioDevices a list of available audio devices
 * @property selectedAudioDevice a currently selected audio device or null
 */
public interface AudioDeviceManager {

    public val availableAudioDevices: List<AudioDevice>

    public val selectedAudioDevice: AudioDevice?

    /**
     * Selects an audio device.
     *
     * @param audioDevice an audio device to select
     * @return true if [audioDevice] was accepted, false otherwise
     */
    public fun selectAudioDevice(audioDevice: AudioDevice): Boolean

    /**
     * Clears currently selected audio device.
     */
    public fun clearAudioDevice()

    /**
     * Registers a [OnSelectedAudioDeviceChangedListener].
     *
     * @param listener a listener to register
     */
    public fun registerOnSelectedAudioDeviceChanged(listener: OnSelectedAudioDeviceChangedListener)

    /**
     * Unregisters a [OnSelectedAudioDeviceChangedListener].
     *
     * @param listener a listener to unregister
     */
    public fun unregisterOnSelectedAudioDeviceChanged(listener: OnSelectedAudioDeviceChangedListener)

    /**
     * Registers a [OnAvailableAudioDevicesChangedListener].
     *
     * @param listener a listener to register
     */
    public fun registerOnAvailableAudioDevicesChangedListener(listener: OnAvailableAudioDevicesChangedListener)

    /**
     * Unregisters a [OnAvailableAudioDevicesChangedListener].
     *
     * @param listener a listener to unregister
     */
    public fun unregisterOnAvailableAudioDevicesChangedListener(listener: OnAvailableAudioDevicesChangedListener)

    /**
     * Disposes this [AudioDeviceManager] and releases any held resources.
     *
     * The instance will become unusable after this call.
     *
     * @throws IllegalStateException if [MediaConnectionFactory] has been disposed
     */
    public fun dispose()

    public fun interface OnSelectedAudioDeviceChangedListener {

        /**
         * Invoked when selected audio device has changed.
         *
         * @param audioDevice an instance of audio device or null
         */
        public fun onSelectedAudioDeviceChange(audioDevice: AudioDevice?)
    }

    public fun interface OnAvailableAudioDevicesChangedListener {

        /**
         * Invoked when available audio devices have changed.
         *
         * @param audioDevices an list of available audio device
         */
        public fun onAvailableAudioDevicesChange(audioDevices: List<AudioDevice>)
    }

    public companion object
}
