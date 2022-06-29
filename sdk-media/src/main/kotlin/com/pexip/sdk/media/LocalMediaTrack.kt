package com.pexip.sdk.media

/**
 * A local media track.
 */
public interface LocalMediaTrack {

    /**
     * Start the capture.
     *
     * Implementations should use [QualityProfile.Medium] if they support changing profiles
     */
    public fun startCapture()

    /**
     * Stops the capture.
     */
    public fun stopCapture()

    /**
     * Registers a [CapturingListener] that will be notified when capturing state changes.
     *
     * @param listener a listener to register
     */
    public fun registerCapturingListener(listener: CapturingListener)

    /**
     * Unregisters a previously registered [CapturingListener].
     *
     * @param listener a listener to unregister
     */
    public fun unregisterCapturingListener(listener: CapturingListener)

    /**
     * Disposes this [LocalMediaTrack] and frees any resources held by it.
     */
    public fun dispose()

    /**
     * A listener that notifies of capturing state changes.
     */
    public fun interface CapturingListener {

        /**
         * Invoked when the capturing state changes.
         *
         * @param capturing true if capturing, false otherwise
         */
        public fun onCapturing(capturing: Boolean)
    }
}
