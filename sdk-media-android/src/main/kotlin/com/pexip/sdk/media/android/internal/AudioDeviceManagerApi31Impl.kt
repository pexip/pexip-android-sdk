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
package com.pexip.sdk.media.android.internal

import android.content.Context
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioManager
import androidx.annotation.RequiresApi
import androidx.core.os.ExecutorCompat
import kotlin.properties.Delegates

@RequiresApi(31)
internal class AudioDeviceManagerApi31Impl(context: Context) :
    AudioDeviceManagerBaseImpl<AudioDeviceApi31Impl>(context),
    AudioManager.OnCommunicationDeviceChangedListener {

    private val executor = ExecutorCompat.create(handler)
    private val audioDeviceCallback = object : AudioDeviceCallback() {

        override fun onAudioDevicesAdded(addedDevices: Array<AudioDeviceInfo>) {
            onAudioDevicesChanged()
        }

        override fun onAudioDevicesRemoved(removedDevices: Array<AudioDeviceInfo>) {
            onAudioDevicesChanged()
        }
    }

    override var availableAudioDevices by Delegates.observable(availableAudioDevices()) { _, old, new ->
        if (old != new) onAvailableAudioDevicesChange(new)
    }
    override var selectedAudioDevice by Delegates.observable(selectedAudioDevice()) { _, old, new ->
        if (old != new) onSelectedAudioDeviceChange(new)
    }

    init {
        audioManager.registerAudioDeviceCallback(audioDeviceCallback, handler)
        audioManager.addOnCommunicationDeviceChangedListener(executor, this)
    }

    override fun doSelectAudioDevice(audioDevice: AudioDeviceApi31Impl): Boolean {
        val success = audioManager.setCommunicationDevice(audioDevice.device)
        if (success) onSelectedAudioDeviceChange()
        return success
    }

    override fun doClearAudioDevice() {
        audioManager.clearCommunicationDevice()
        onSelectedAudioDeviceChange()
    }

    override fun doDispose() {
        audioManager.removeOnCommunicationDeviceChangedListener(this)
        audioManager.unregisterAudioDeviceCallback(audioDeviceCallback)
    }

    override fun onCommunicationDeviceChanged(device: AudioDeviceInfo?) {
        onAudioDevicesChanged()
    }

    private fun onAudioDevicesChanged() {
        onAvailableAudioDevicesChange()
        onSelectedAudioDeviceChange()
        if (audioManager.communicationDevice == null) {
            audioManager.clearCommunicationDevice()
        }
    }

    private fun onAvailableAudioDevicesChange() {
        availableAudioDevices = availableAudioDevices()
    }

    private fun onSelectedAudioDeviceChange() {
        selectedAudioDevice = selectedAudioDevice()
    }

    private fun availableAudioDevices() =
        AudioDeviceApi31Impl.from(audioManager.availableCommunicationDevices)

    private fun selectedAudioDevice() =
        AudioDeviceApi31Impl.from(audioManager.communicationDevice)
}
