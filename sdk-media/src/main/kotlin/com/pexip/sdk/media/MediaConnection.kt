package com.pexip.sdk.media

public interface MediaConnection {

    public fun start()

    public fun dispose()

    @Deprecated(message = "Use localAudioTrack version.", level = DeprecationLevel.ERROR)
    public fun sendMainAudio(): Unit = throw UnsupportedOperationException("Deprecated.")

    public fun sendMainAudio(localAudioTrack: LocalAudioTrack)

    public fun sendMainVideo()

    public fun sendMainVideo(deviceName: String)

    public fun startMainCapture()

    public fun stopMainCapture()

    public fun startPresentationReceive()

    public fun stopPresentationReceive()

    public fun registerMainCapturingListener(listener: CapturingListener)

    public fun unregisterMainCapturingListener(listener: CapturingListener)
}
