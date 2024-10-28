/*
 * Copyright 2022-2024 Pexip AS
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

import com.pexip.sdk.media.LocalAudioTrack
import com.pexip.sdk.media.LocalMediaTrack
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.webrtc.AudioSource
import org.webrtc.AudioTrack
import java.util.concurrent.CopyOnWriteArraySet
import kotlin.coroutines.CoroutineContext

internal class WebRtcLocalAudioTrack(
    private val audioDeviceModule: AudioDeviceModuleWrapper,
    private val audioSource: AudioSource,
    internal val audioTrack: AudioTrack,
    private val scope: CoroutineScope,
    signalingDispatcher: CoroutineDispatcher,
) : LocalAudioTrack {

    internal constructor(
        audioDeviceModule: AudioDeviceModuleWrapper,
        audioSource: AudioSource,
        audioTrack: AudioTrack,
        coroutineContext: CoroutineContext,
        signalingDispatcher: CoroutineDispatcher,
    ) : this(
        audioDeviceModule = audioDeviceModule,
        audioSource = audioSource,
        audioTrack = audioTrack,
        scope = CoroutineScope(coroutineContext),
        signalingDispatcher = signalingDispatcher,
    )

    private val listeners = CopyOnWriteArraySet<LocalMediaTrack.CapturingListener>()

    override val capturing: Boolean
        get() = !audioDeviceModule.microphoneMute.value

    init {
        scope.launch {
            try {
                audioDeviceModule.microphoneMute
                    .drop(1)
                    .onEach { microphoneMute ->
                        listeners.forEach { it.safeOnCapturing(!microphoneMute) }
                    }
                    .flowOn(signalingDispatcher)
                    .collect()
            } finally {
                withContext(NonCancellable) {
                    listeners.clear()
                    audioTrack.dispose()
                    audioSource.dispose()
                }
            }
        }
    }

    override fun startCapture() {
        audioDeviceModule.setMicrophoneMute(false)
    }

    override fun stopCapture() {
        audioDeviceModule.setMicrophoneMute(true)
    }

    override fun registerCapturingListener(listener: LocalMediaTrack.CapturingListener) {
        listeners += listener
    }

    override fun unregisterCapturingListener(listener: LocalMediaTrack.CapturingListener) {
        listeners -= listener
    }

    override fun dispose() {
        scope.cancel()
    }
}
