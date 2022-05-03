package com.pexip.sdk.media

public interface MediaConnectionFactory {

    public fun createLocalAudioTrack(): LocalAudioTrack

    public fun createCameraVideoTrack(): CameraVideoTrack

    public fun createCameraVideoTrack(deviceName: String): CameraVideoTrack

    public fun createMediaConnection(config: MediaConnectionConfig): MediaConnection

    public fun dispose()
}
