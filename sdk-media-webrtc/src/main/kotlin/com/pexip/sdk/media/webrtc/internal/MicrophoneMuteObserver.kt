package com.pexip.sdk.media.webrtc.internal

import android.content.Context
import android.media.AudioManager
import android.os.Build
import androidx.core.content.getSystemService

internal abstract class MicrophoneMuteObserver(context: Context) {

    fun interface Callback {

        fun onMicrophoneMuteChange(microphoneMute: Boolean)
    }

    private val initialMicrophoneMute = microphoneMute

    protected val audioManager = context.getSystemService<AudioManager>()!!

    var microphoneMute: Boolean
        get() = audioManager.isMicrophoneMute
        set(value) = doSetMicrophoneMute(value)

    fun dispose() {
        doDispose()
        microphoneMute = initialMicrophoneMute
    }

    protected abstract fun doDispose()

    protected abstract fun doSetMicrophoneMute(microphoneMute: Boolean)
}

internal fun MicrophoneMuteObserver(context: Context, callback: MicrophoneMuteObserver.Callback) =
    when {
        Build.VERSION.SDK_INT >= 28 -> MicrophoneMuteObserverApi28Impl(context, callback)
        else -> MicrophoneMuteObserverApi21Impl(context, callback)
    }
