package com.pexip.sdk.media

public interface MediaConnection {

    public fun start()

    public fun dispose()

    public fun sendMainAudio()

    public fun sendMainVideo()

    public fun sendMainVideo(deviceName: String)

    public fun startMainCapture()

    public fun stopMainCapture()

    public fun registerMainCapturingListener(listener: CapturingListener)

    public fun unregisterMainCapturingListener(listener: CapturingListener)
}
