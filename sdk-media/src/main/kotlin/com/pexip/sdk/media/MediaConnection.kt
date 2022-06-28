package com.pexip.sdk.media

public interface MediaConnection {

    public fun start()

    public fun dispose()

    @Deprecated(
        message = "Use setMainAudioTrack instead.",
        replaceWith = ReplaceWith("setMainAudioTrack(localAudioTrack)")
    )
    public fun sendMainAudio(localAudioTrack: LocalAudioTrack)

    @Deprecated(
        message = "Use setMainVideoTrack instead.",
        replaceWith = ReplaceWith("setMainVideoTrack(localVideoTrack)")
    )
    public fun sendMainVideo(localVideoTrack: LocalVideoTrack)

    public fun setMainAudioTrack(localAudioTrack: LocalAudioTrack?)

    public fun setMainVideoTrack(localVideoTrack: LocalVideoTrack?)

    public fun setPresentationVideoTrack(localVideoTrack: LocalVideoTrack?)

    public fun startPresentationReceive()

    public fun stopPresentationReceive()

    public fun registerMainRemoteVideoTrackListener(listener: RemoteVideoTrackListener)

    public fun unregisterMainRemoteVideoTrackListener(listener: RemoteVideoTrackListener)

    public fun registerPresentationRemoteVideoTrackListener(listener: RemoteVideoTrackListener)

    public fun unregisterPresentationRemoteVideoTrackListener(listener: RemoteVideoTrackListener)

    public fun interface RemoteVideoTrackListener {

        public fun onRemoteVideoTrack(videoTrack: VideoTrack?)
    }
}
