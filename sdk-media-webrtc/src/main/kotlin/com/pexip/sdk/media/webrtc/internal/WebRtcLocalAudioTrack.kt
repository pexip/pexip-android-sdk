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
import com.pexip.sdk.media.LocalAudioTrack
import com.pexip.sdk.media.LocalMediaTrack
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.webrtc.AudioSource
import org.webrtc.AudioTrack
import java.util.concurrent.CopyOnWriteArraySet

internal class WebRtcLocalAudioTrack(
    context: Context,
    private val audioSource: AudioSource,
    internal val audioTrack: AudioTrack,
    private val scope: CoroutineScope,
    signalingDispatcher: CoroutineDispatcher,
) : LocalAudioTrack {

    private val capturingListeners = CopyOnWriteArraySet<LocalMediaTrack.CapturingListener>()
    private val microphoneMuteObserver = MicrophoneMuteObserver(context) { microphoneMute ->
        scope.launch(signalingDispatcher) {
            capturingListeners.forEach { it.safeOnCapturing(!microphoneMute) }
        }
    }

    override val capturing: Boolean
        get() = !microphoneMuteObserver.microphoneMute

    init {
        scope.launch {
            try {
                awaitCancellation()
            } finally {
                withContext(NonCancellable) {
                    capturingListeners.clear()
                    microphoneMuteObserver.dispose()
                    audioTrack.dispose()
                    audioSource.dispose()
                }
            }
        }
    }

    override fun startCapture() {
        scope.launch { microphoneMuteObserver.microphoneMute = false }
    }

    override fun stopCapture() {
        scope.launch { microphoneMuteObserver.microphoneMute = true }
    }

    override fun registerCapturingListener(listener: LocalMediaTrack.CapturingListener) {
        capturingListeners += listener
    }

    override fun unregisterCapturingListener(listener: LocalMediaTrack.CapturingListener) {
        capturingListeners -= listener
    }

    override fun dispose() {
        scope.cancel()
    }
}
