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

import android.media.AudioDeviceInfo
import androidx.annotation.RequiresApi
import com.pexip.sdk.media.AudioDevice

@RequiresApi(31)
@JvmInline
internal value class AudioDeviceApi31Impl private constructor(val device: AudioDeviceInfo) :
    AudioDevice {

    override val type: AudioDevice.Type
        get() = supportedTypes.getValue(device.type)

    override val name: String
        get() = device.productName.toString()

    override fun toString(): String = buildString {
        append("AudioDeviceApi31Impl(")
        append("type=")
        append(type)
        append(",name=")
        append(name)
        append(")")
    }

    companion object {

        private val supportedTypes = buildMap {
            put(AudioDeviceInfo.TYPE_BUILTIN_EARPIECE, AudioDevice.Type.BUILTIN_EARPIECE)
            put(AudioDeviceInfo.TYPE_BUILTIN_SPEAKER, AudioDevice.Type.BUILTIN_SPEAKER)
            put(AudioDeviceInfo.TYPE_BLUETOOTH_A2DP, AudioDevice.Type.BLUETOOTH_A2DP)
            put(AudioDeviceInfo.TYPE_BLUETOOTH_SCO, AudioDevice.Type.BLUETOOTH_SCO)
            put(AudioDeviceInfo.TYPE_WIRED_HEADSET, AudioDevice.Type.WIRED_HEADSET)
        }

        fun from(devices: List<AudioDeviceInfo>): List<AudioDeviceApi31Impl> =
            devices.mapNotNull(::from)

        fun from(device: AudioDeviceInfo?): AudioDeviceApi31Impl? = device
            ?.takeIf { it.type in supportedTypes.keys }
            ?.let(::AudioDeviceApi31Impl)
    }
}
