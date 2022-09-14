package com.pexip.sdk.media.webrtc.internal

import android.content.Context

internal class MicrophoneMuteObserverApi21Impl(context: Context, private val callback: Callback) :
    MicrophoneMuteObserver(context) {

    override fun doSetMicrophoneMute(microphoneMute: Boolean) {
        val previousValue = audioManager.isMicrophoneMute
        if (previousValue == microphoneMute) return
        audioManager.isMicrophoneMute = microphoneMute
        val newValue = audioManager.isMicrophoneMute
        if (newValue == microphoneMute) callback.onMicrophoneMuteChange(microphoneMute)
    }

    override fun dispose() {
        // noop
    }
}
