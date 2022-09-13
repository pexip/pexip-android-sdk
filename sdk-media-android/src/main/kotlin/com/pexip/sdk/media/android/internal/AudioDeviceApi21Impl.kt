package com.pexip.sdk.media.android.internal

import com.pexip.sdk.media.AudioDevice

internal data class AudioDeviceApi21Impl(
    override val type: AudioDevice.Type,
    override val name: String? = null,
) : AudioDevice
