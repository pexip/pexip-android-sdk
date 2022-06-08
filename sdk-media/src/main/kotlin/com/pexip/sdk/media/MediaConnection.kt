package com.pexip.sdk.media

public interface MediaConnection {

    public fun start()

    public fun dispose()

    public fun sendMainAudio(localAudioTrack: LocalAudioTrack)

    public fun sendMainVideo(localVideoTrack: LocalVideoTrack)

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
