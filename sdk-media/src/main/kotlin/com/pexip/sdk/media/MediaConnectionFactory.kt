package com.pexip.sdk.media

/**
 * Main entry point to the [MediaConnection] API for clients.
 *
 * Note: use [LocalAudioTrackFactory] and [CameraVideoTrackFactory] directly as
 * [MediaConnectionFactory] won't eventually implement these interfaces.
 */
public interface MediaConnectionFactory : LocalAudioTrackFactory, CameraVideoTrackFactory {

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
