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

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import androidx.annotation.RequiresApi

@RequiresApi(28)
internal class MicrophoneMuteObserverApi28Impl(
    private val context: Context,
    private val callback: Callback,
) : MicrophoneMuteObserver(context) {

    private val filter = IntentFilter(AudioManager.ACTION_MICROPHONE_MUTE_CHANGED)
    private val receiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            callback.onMicrophoneMuteChange(microphoneMute)
        }
    }

    init {
        context.registerReceiver(receiver, filter)
    }

    override fun doSetMicrophoneMute(microphoneMute: Boolean) {
        audioManager.isMicrophoneMute = microphoneMute
    }

    override fun doDispose() {
        try {
            context.unregisterReceiver(receiver)
        } catch (e: IllegalArgumentException) {
            // noop
        }
    }
}
