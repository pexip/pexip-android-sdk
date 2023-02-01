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
