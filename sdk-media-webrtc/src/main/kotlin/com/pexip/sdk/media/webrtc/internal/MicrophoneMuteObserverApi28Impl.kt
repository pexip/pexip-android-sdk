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

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import androidx.annotation.RequiresApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@RequiresApi(28)
internal class MicrophoneMuteObserverApi28Impl(
    context: Context,
    scope: CoroutineScope,
) : MicrophoneMuteObserver(context, scope) {

    override val microphoneMute: StateFlow<Boolean> = context.microphoneMuteChange()
        .map { audioManager.isMicrophoneMute }
        .stateIn(scope, SharingStarted.Eagerly, audioManager.isMicrophoneMute)

    override fun setMicrophoneMute(mute: Boolean) {
        scope.launch { audioManager.isMicrophoneMute = mute }
    }

    private fun Context.microphoneMuteChange() = callbackFlow {
        val filter = IntentFilter(AudioManager.ACTION_MICROPHONE_MUTE_CHANGED)
        val receiver = object : BroadcastReceiver() {

            override fun onReceive(context: Context, intent: Intent) {
                trySend(intent)
            }
        }
        registerReceiver(receiver, filter)
        awaitClose {
            try {
                unregisterReceiver(receiver)
            } catch (e: IllegalArgumentException) {
                // noop
            }
        }
    }
}
