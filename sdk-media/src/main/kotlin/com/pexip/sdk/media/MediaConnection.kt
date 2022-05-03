package com.pexip.sdk.media

import androidx.annotation.MainThread

public interface MediaConnection {

    public fun start()

    public fun dispose()

    @Deprecated(message = "Use localAudioTrack version.", level = DeprecationLevel.ERROR)
    public fun sendMainAudio(): Unit = deprecated()

    public fun sendMainAudio(localAudioTrack: LocalAudioTrack)

    @Deprecated("Use localVideoTrack version.", level = DeprecationLevel.ERROR)
    public fun sendMainVideo(): Unit = deprecated()

    @Deprecated("Use localVideoTrack version.", level = DeprecationLevel.ERROR)
    public fun sendMainVideo(deviceName: String): Unit = deprecated()

    public fun sendMainVideo(localVideoTrack: LocalVideoTrack)

    @Deprecated("Use localVideoTrack version.", level = DeprecationLevel.ERROR)
    public fun startMainCapture(): Unit = deprecated()

    @Deprecated("Use localVideoTrack version.", level = DeprecationLevel.ERROR)
    public fun stopMainCapture(): Unit = deprecated()

    public fun startPresentationReceive()

    public fun stopPresentationReceive()

    public fun registerMainRemoteVideoTrackListener(listener: RemoteVideoTrackListener)

    public fun unregisterMainRemoteVideoTrackListener(listener: RemoteVideoTrackListener)

    public fun registerPresentationRemoteVideoTrackListener(listener: RemoteVideoTrackListener)

    public fun unregisterPresentationRemoteVideoTrackListener(listener: RemoteVideoTrackListener)

    public fun interface RemoteVideoTrackListener {

        @MainThread
        public fun onRemoteVideoTrack(videoTrack: VideoTrack?)
    }

    private fun deprecated(): Nothing = throw UnsupportedOperationException("Deprecated.")
}
