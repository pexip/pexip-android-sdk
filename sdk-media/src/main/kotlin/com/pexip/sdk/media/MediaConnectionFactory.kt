package com.pexip.sdk.media

/**
 * Main entry point to the [MediaConnection] API for clients.
 */
public interface MediaConnectionFactory {

    /**
     * Creates a [LocalAudioTrack].
     */
    public fun createLocalAudioTrack(): LocalAudioTrack

    /**
     * Creates a [CameraVideoTrack] for the best available camera.
     *
     * Best available camera is determined by the following order:
     * 1. First front-facing camera
     * 2. First back-facing camera
     * 3. First available camera
     */
    public fun createCameraVideoTrack(): CameraVideoTrack

    /**
     * Creates a [CameraVideoTrack] for specific [deviceName].
     *
     * @param deviceName a device name that should be opened
     */
    public fun createCameraVideoTrack(deviceName: String): CameraVideoTrack

    /**
     * Creates a [MediaConnection] with the specified [config].
     */
    public fun createMediaConnection(config: MediaConnectionConfig): MediaConnection

    /**
     * Disposes this [MediaConnectionFactory] and releases any held resources.
     *
     * The instance will become unusable after this call.
     */
    public fun dispose()
}
