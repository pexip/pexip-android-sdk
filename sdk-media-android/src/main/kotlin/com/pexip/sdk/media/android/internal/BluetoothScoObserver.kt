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
            AudioManager.SCO_AUDIO_STATE_DISCONNECTED
        )
        connected = state == AudioManager.SCO_AUDIO_STATE_CONNECTED
    }

    override fun close() {
        receiver.close()
    }
}
