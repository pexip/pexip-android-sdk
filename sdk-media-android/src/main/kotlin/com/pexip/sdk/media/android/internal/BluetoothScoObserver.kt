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
package com.pexip.sdk.media.android.internal

import android.content.Context
import android.content.IntentFilter
import android.media.AudioManager
import android.os.Handler
import java.io.Closeable
import kotlin.properties.Delegates

internal class BluetoothScoObserver(
    context: Context,
    handler: Handler,
    private val onConnectedChange: (connected: Boolean) -> Unit,
) : Closeable {

    var connected by Delegates.observable(false) { _, old, new ->
        if (old != new) onConnectedChange(new)
    }
        private set

    private val filter = IntentFilter(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED)
    private val receiver = context.registerReceiver(filter, handler) { _, intent ->
        val state = intent.getIntExtra(
            AudioManager.EXTRA_SCO_AUDIO_STATE,
            AudioManager.SCO_AUDIO_STATE_DISCONNECTED,
        )
        connected = state == AudioManager.SCO_AUDIO_STATE_CONNECTED
    }

    override fun close() {
        receiver.close()
    }
}
