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
