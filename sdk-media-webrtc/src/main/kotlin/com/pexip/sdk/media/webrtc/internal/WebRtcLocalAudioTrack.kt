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

import android.content.Context
import com.pexip.sdk.media.LocalAudioTrack
import com.pexip.sdk.media.LocalMediaTrack
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.withContext
import org.webrtc.AudioSource
import org.webrtc.AudioTrack
import java.util.concurrent.CopyOnWriteArraySet
import kotlin.coroutines.CoroutineContext

internal class WebRtcLocalAudioTrack(
    context: Context,
    private val audioSource: AudioSource,
    internal val audioTrack: AudioTrack,
    coroutineContext: CoroutineContext,
    signalingDispatcher: CoroutineDispatcher,
) : LocalAudioTrack {

    private val scope = CoroutineScope(coroutineContext)

    private val listeners = CopyOnWriteArraySet<LocalMediaTrack.CapturingListener>()
    private val microphoneMuteObserver =
        context.microphoneMuteObserverIn(scope + signalingDispatcher)

    override val capturing: Boolean
        get() = !microphoneMuteObserver.microphoneMute.value

    init {
        scope.launch {
            try {
                microphoneMuteObserver.microphoneMute
                    .drop(1)
                    .onEach { microphoneMute ->
                        listeners.forEach { it.safeOnCapturing(!microphoneMute) }
                    }
                    .flowOn(signalingDispatcher)
                    .launchIn(this)
                awaitCancellation()
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
        microphoneMuteObserver.setMicrophoneMute(false)
    }

    override fun stopCapture() {
        microphoneMuteObserver.setMicrophoneMute(true)
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
