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
package com.pexip.sdk.sample.di

import android.app.Application
import com.pexip.sdk.media.AudioDevice
import com.pexip.sdk.media.AudioDeviceManager
import com.pexip.sdk.media.android.create
import com.pexip.sdk.sample.log
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object AudioDeviceManagerModule {

    @Provides
    fun Application.provideAudioDeviceManager(): AudioDeviceManager =
        LoggingAudioDeviceManager(AudioDeviceManager.create(this))

    private class LoggingAudioDeviceManager(private val delegate: AudioDeviceManager) :
        AudioDeviceManager {

        private val tag = delegate.javaClass.simpleName

        override val availableAudioDevices: List<AudioDevice>
            get() {
                val availableAudioDevices = delegate.availableAudioDevices
                log(tag) { "availableAudioDevices=$availableAudioDevices" }
                return availableAudioDevices
            }

        override val selectedAudioDevice: AudioDevice?
            get() {
                val selectedAudioDevice = delegate.selectedAudioDevice
                log(tag) { "selectedAudioDevice=$selectedAudioDevice" }
                return selectedAudioDevice
            }

        override fun selectAudioDevice(audioDevice: AudioDevice): Boolean {
            val success = delegate.selectAudioDevice(audioDevice)
            log(tag) { "selectAudioDevice($audioDevice): $success" }
            return success
        }

        override fun clearAudioDevice() {
            log(tag) { "clearAudioDevice()" }
            delegate.clearAudioDevice()
        }

        override fun registerOnSelectedAudioDeviceChanged(listener: AudioDeviceManager.OnSelectedAudioDeviceChangedListener) {
            log(tag) { "registerOnSelectedAudioDeviceChanged()" }
            val l = OnSelectedAudioDeviceChangedListener(tag, listener)
            delegate.registerOnSelectedAudioDeviceChanged(l)
        }

        override fun unregisterOnSelectedAudioDeviceChanged(listener: AudioDeviceManager.OnSelectedAudioDeviceChangedListener) {
            log(tag) { "unregisterOnSelectedAudioDeviceChanged()" }
            val l = OnSelectedAudioDeviceChangedListener(tag, listener)
            delegate.unregisterOnSelectedAudioDeviceChanged(l)
        }

        override fun registerOnAvailableAudioDevicesChangedListener(listener: AudioDeviceManager.OnAvailableAudioDevicesChangedListener) {
            log(tag) { "registerOnAvailableAudioDevicesChangedListener()" }
            val l = OnAvailableAudioDevicesChangedListener(tag, listener)
            delegate.registerOnAvailableAudioDevicesChangedListener(l)
        }

        override fun unregisterOnAvailableAudioDevicesChangedListener(listener: AudioDeviceManager.OnAvailableAudioDevicesChangedListener) {
            log(tag) { "unregisterOnAvailableAudioDevicesChangedListener()" }
            val l = OnAvailableAudioDevicesChangedListener(tag, listener)
            delegate.unregisterOnAvailableAudioDevicesChangedListener(l)
        }

        override fun dispose() {
            log(tag) { "dispose()" }
            delegate.dispose()
        }

        private class OnSelectedAudioDeviceChangedListener(
            private val tag: String,
            private val listener: AudioDeviceManager.OnSelectedAudioDeviceChangedListener,
        ) : AudioDeviceManager.OnSelectedAudioDeviceChangedListener {

            override fun onSelectedAudioDeviceChange(audioDevice: AudioDevice?) {
                log(tag) { "onSelectedAudioDeviceChanged($audioDevice)" }
                listener.onSelectedAudioDeviceChange(audioDevice)
            }

            override fun equals(other: Any?): Boolean = listener == other

            override fun hashCode(): Int = listener.hashCode()
        }

        private class OnAvailableAudioDevicesChangedListener(
            private val tag: String,
            private val listener: AudioDeviceManager.OnAvailableAudioDevicesChangedListener,
        ) : AudioDeviceManager.OnAvailableAudioDevicesChangedListener {

            override fun onAvailableAudioDevicesChange(audioDevices: List<AudioDevice>) {
                log(tag) { "onAvailableAudioDevicesChanged($audioDevices)" }
                listener.onAvailableAudioDevicesChange(audioDevices)
            }

            override fun equals(other: Any?): Boolean = listener == other

            override fun hashCode(): Int = listener.hashCode()
        }
    }
}
