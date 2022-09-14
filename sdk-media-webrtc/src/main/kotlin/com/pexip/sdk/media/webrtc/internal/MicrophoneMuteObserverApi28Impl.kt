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

    override fun dispose() {
        try {
            context.unregisterReceiver(receiver)
        } catch (e: IllegalArgumentException) {
            // noop
        }
    }
}
