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
 * An audio device.
 *
 * @property type this audio devices type
 * @property name a name of this audio device fit for display
 */
public interface AudioDevice {

    public val type: Type

    public val name: String?

    public enum class Type {

        /**
         * An audio device type describing the speaker system (i.e. a mono speaker or stereo speakers)
         * built in a device.
         */
        BUILTIN_SPEAKER,

        /**
         * An audio device type describing the attached earphone speaker.
         */
        BUILTIN_EARPIECE,

        /**
         * An audio device type describing a headset, which is the combination of a headphones and
         * microphone.
         */
        WIRED_HEADSET,

        /**
         * An audio device type describing a Bluetooth device supporting the A2DP profile.
         */
        BLUETOOTH_A2DP,

        /**
         * An audio device type describing a Bluetooth device typically used for telephony.
         */
        BLUETOOTH_SCO,
    }
}
