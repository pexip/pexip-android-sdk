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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal class MicrophoneMuteObserverApi21Impl(
    context: Context,
    scope: CoroutineScope,
) : MicrophoneMuteObserver(context, scope) {

    private val _microphoneMute = MutableStateFlow(audioManager.isMicrophoneMute)

    override val microphoneMute: StateFlow<Boolean> = _microphoneMute.asStateFlow()

    override fun setMicrophoneMute(mute: Boolean) {
        scope.launch {
            audioManager.isMicrophoneMute = mute
            _microphoneMute.emit(audioManager.isMicrophoneMute)
        }
    }
}
