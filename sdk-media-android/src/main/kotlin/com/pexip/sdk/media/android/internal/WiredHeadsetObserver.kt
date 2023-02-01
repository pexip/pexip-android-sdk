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

internal class WiredHeadsetObserver(
    context: Context,
    handler: Handler,
    onConnectedChange: (connected: Boolean, name: String?) -> Unit,
) : Closeable {

    private val filter = IntentFilter(AudioManager.ACTION_HEADSET_PLUG)
    private val receiver = context.registerReceiver(filter, handler) { _, intent ->
        val plugged = intent.getIntExtra("state", 0) != 0
        val name = intent.getStringExtra("name") ?: intent.getStringExtra("portName")
        onConnectedChange(plugged, name)
    }

    override fun close() {
        receiver.close()
    }
}
