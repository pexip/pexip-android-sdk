package com.pexip.sdk.media.android

import android.content.Context
import android.os.Build
import com.pexip.sdk.media.AudioDeviceManager
import com.pexip.sdk.media.android.internal.AudioDeviceManagerApi21Impl
import com.pexip.sdk.media.android.internal.AudioDeviceManagerApi31Impl

/**
 * Creates an instance of [AudioDeviceManager] based on the current platform version.
 *
 * Behavior may differ depending on the underlying platform.
 *
 * @param context a context
 * @return an instance of [AudioDeviceManager]
 */
public fun AudioDeviceManager.Companion.create(context: Context): AudioDeviceManager = when {
    Build.VERSION.SDK_INT >= 31 -> AudioDeviceManagerApi31Impl(context)
    else -> AudioDeviceManagerApi21Impl(context)
}
