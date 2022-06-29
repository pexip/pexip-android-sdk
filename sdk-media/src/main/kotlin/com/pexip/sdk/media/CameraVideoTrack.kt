package com.pexip.sdk.media

/**
 * A camera video track.
 */
public interface CameraVideoTrack : LocalVideoTrack {

    /**
     * Attempts to switch a camera to the opposite one (i.e. from front to back and vice versa).
     *
     * @param callback a callback that will be invoked on either success or failure
     */
    public fun switchCamera(callback: SwitchCameraCallback)

    public interface SwitchCameraCallback {

        /**
         * Invoked when camera switch completed successfully.
         *
         * @param front true if the newly switched camera is front-facing, false otherwise
         */
        public fun onSuccess(front: Boolean)

        /**
         * Invoked when camera switch failed.
         *
         * @param error an error that occurred during the switch
         */
        public fun onFailure(error: String)
    }
}
