/*
 * Copyright 2024 Pexip AS
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

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.webrtc.audio.AudioDeviceModule

internal class AudioDeviceModuleWrapper(private val module: AudioDeviceModule) :
    AudioDeviceModule by module {

    private val _microphoneMute = MutableStateFlow(false)

    val microphoneMute: StateFlow<Boolean> = _microphoneMute.asStateFlow()

    init {
        module.setMicrophoneMute(false)
    }

    override fun setMicrophoneMute(microphoneMute: Boolean) {
        module.setMicrophoneMute(microphoneMute)
        _microphoneMute.update { microphoneMute }
    }
}
