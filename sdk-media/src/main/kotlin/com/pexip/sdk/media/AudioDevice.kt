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
        BLUETOOTH_SCO
    }
}
