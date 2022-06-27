package com.pexip.sdk.media

/**
 * Main entry point to the [MediaConnection] API for clients.
 */
public interface MediaConnectionFactory {

    /**
     * Creates a [LocalAudioTrack].
     *
     * @return a [LocalAudioTrack]
     * @throws IllegalStateException if [MediaConnectionFactory] has been disposed
     */
    public fun createLocalAudioTrack(): LocalAudioTrack

    /**
     * Creates a [CameraVideoTrack] for the best available camera.
     *
     * Best available camera is determined by the following order:
     * 1. First front-facing camera
     * 2. First back-facing camera
     * 3. First available camera
     *
     * @return a [CameraVideoTrack]
     * @throws IllegalStateException if no camera is available
     * @throws IllegalStateException if [MediaConnectionFactory] has been disposed
     */
    public fun createCameraVideoTrack(): CameraVideoTrack

    /**
     * Creates a [CameraVideoTrack] for specific [deviceName].
     *
     * @param deviceName a device name that should be opened
     * @return a [CameraVideoTrack]
     * @throws IllegalStateException if [deviceName] is not available
     * @throws IllegalStateException if [MediaConnectionFactory] has been disposed
     */
    public fun createCameraVideoTrack(deviceName: String): CameraVideoTrack

    /**
     * Creates a [MediaConnection] with the specified [config].
     *
     * @return a [MediaConnection]
     * @throws IllegalStateException if [MediaConnectionFactory] has been disposed
     */
    public fun createMediaConnection(config: MediaConnectionConfig): MediaConnection

    /**
     * Disposes this [MediaConnectionFactory] and releases any held resources.
     *
     * The instance will become unusable after this call.
     *
     * @throws IllegalStateException if [MediaConnectionFactory] has been disposed
     */
    public fun dispose()
}
