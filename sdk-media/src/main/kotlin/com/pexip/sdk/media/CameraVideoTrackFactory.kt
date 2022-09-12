package com.pexip.sdk.media

public interface CameraVideoTrackFactory {

    /**
     * Returns all available device names.
     *
     * @return a list of available device names
     */
    public fun getDeviceNames(): List<String>

    /**
     * Checks whether the provided device name belongs to a front-facing camera.
     *
     * @return true if the camera is front-facing, false otherwise
     */
    public fun isFrontFacing(deviceName: String): Boolean

    /**
     * Checks whether the provided device name belongs to a back-facing camera.
     *
     * @return true if the camera is back-facing, false otherwise
     */
    public fun isBackFacing(deviceName: String): Boolean

    /**
     * Returns a list of supported quality profiles for a specific camera.
     *
     * @return a list of supported quality profiles
     */
    public fun getQualityProfiles(deviceName: String): List<QualityProfile>

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
    @Deprecated("Use createCameraVideoTrack() that also accepts a Callback.")
    public fun createCameraVideoTrack(): CameraVideoTrack

    /**
     * Creates a [CameraVideoTrack] for specific [deviceName].
     *
     * @param deviceName a device name that should be opened
     * @return a [CameraVideoTrack]
     * @throws IllegalStateException if [deviceName] is not available
     * @throws IllegalStateException if [MediaConnectionFactory] has been disposed
     */
    @Deprecated("Use createCameraVideoTrack() that also accepts a Callback.")
    public fun createCameraVideoTrack(deviceName: String): CameraVideoTrack

    /**
     * Creates a [CameraVideoTrack] for the best available camera.
     *
     * Best available camera is determined by the following order:
     * 1. First front-facing camera
     * 2. First back-facing camera
     * 3. First available camera
     *
     * @param callback a callback used to signal various camera events
     * @return a [CameraVideoTrack]
     * @throws IllegalStateException if no camera is available
     * @throws IllegalStateException if [CameraVideoTrackFactory] has been disposed
     */
    public fun createCameraVideoTrack(callback: CameraVideoTrack.Callback): CameraVideoTrack

    /**
     * Creates a [CameraVideoTrack] for specific [deviceName].
     *
     * @param deviceName a device name that should be opened
     * @param callback a callback used to signal various camera events
     * @return a [CameraVideoTrack]
     * @throws IllegalStateException if [deviceName] is not available
     * @throws IllegalStateException if [CameraVideoTrackFactory] has been disposed
     */
    public fun createCameraVideoTrack(
        deviceName: String,
        callback: CameraVideoTrack.Callback,
    ): CameraVideoTrack
}
