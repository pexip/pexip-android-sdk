package com.pexip.sdk.media.webrtc.internal

internal interface AudioHandler {

    var microphoneMute: Boolean

    fun start()

    fun stop()

    fun registerMicrophoneMuteListener(listener: MicrophoneMuteListener)

    fun unregisterMicrophoneMuteListener(listener: MicrophoneMuteListener)

    fun interface MicrophoneMuteListener {

        fun onMicrophoneMute(microphoneMute: Boolean)
    }
}
