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
package com.pexip.sdk.media.webrtc.internal

import android.content.Context
import android.media.AudioManager
import android.os.Build
import androidx.core.content.getSystemService

internal abstract class MicrophoneMuteObserver(context: Context) {

    fun interface Callback {

        fun onMicrophoneMuteChange(microphoneMute: Boolean)
    }

    protected val audioManager = context.getSystemService<AudioManager>()!!

    var microphoneMute: Boolean
        get() = audioManager.isMicrophoneMute
        set(value) = doSetMicrophoneMute(value)

    private val initialMicrophoneMute = microphoneMute

    fun dispose() {
        doDispose()
        microphoneMute = initialMicrophoneMute
    }

    protected abstract fun doDispose()

    protected abstract fun doSetMicrophoneMute(microphoneMute: Boolean)
}

internal fun MicrophoneMuteObserver(context: Context, callback: MicrophoneMuteObserver.Callback) =
    when {
        Build.VERSION.SDK_INT >= 28 -> MicrophoneMuteObserverApi28Impl(context, callback)
        else -> MicrophoneMuteObserverApi21Impl(context, callback)
    }
