/*
 * Copyright 2022-2023 Pexip AS
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal abstract class MicrophoneMuteObserver(
    context: Context,
    protected val scope: CoroutineScope,
) {

    protected val audioManager = context.getSystemService<AudioManager>()!!

    abstract val microphoneMute: StateFlow<Boolean>

    init {
        scope.launch {
            val initialMicrophoneMute = audioManager.isMicrophoneMute
            try {
                awaitCancellation()
            } finally {
                withContext(NonCancellable) { setMicrophoneMute(initialMicrophoneMute) }
            }
        }
    }

    abstract fun setMicrophoneMute(mute: Boolean)
}

internal fun Context.microphoneMuteObserverIn(scope: CoroutineScope) = when {
    Build.VERSION.SDK_INT >= 28 -> MicrophoneMuteObserverApi28Impl(this, scope)
    else -> MicrophoneMuteObserverApi21Impl(this, scope)
}
