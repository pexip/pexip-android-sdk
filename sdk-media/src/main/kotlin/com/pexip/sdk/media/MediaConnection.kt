package com.pexip.sdk.media

/**
 * A media connection.
 */
public interface MediaConnection {

    /**
     * Starts the negotiation process.
     *
     * Can be invoked only once.
     */
    public fun start()

    /**
     * Disposes this [MediaConnection] and frees all held resources associated with it.
     */
    public fun dispose()

    /**
     * Sets the main audio track.
     *
     * This call also enables incoming audio.
     *
     * @param localAudioTrack a local audio track to transmit
     */
    public fun setMainAudioTrack(localAudioTrack: LocalAudioTrack?)

    /**
     * Sets the main video track.
     *
     * This call also enables incoming video.
     *
     * @param localVideoTrack a local video track to transmit
     */
    public fun setMainVideoTrack(localVideoTrack: LocalVideoTrack?)

    /**
     * Sets the presentation video track.
     *
     * This call will "steal" any ongoing presentation. Pass `null` to remove the presentation
     * video track.
     *
     * @param localVideoTrack a local video track to transmit
     */
    public fun setPresentationVideoTrack(localVideoTrack: LocalVideoTrack?)

    /**
     * Allows this [MediaConnection] to receive ongoing remote presentation.
     */
    public fun startPresentationReceive()

    /**
     * Disables the ability to receive ongoing remote presentation.
     */
    public fun stopPresentationReceive()

    /**
     * Sends DTMF digits to this [MediaConnection].
     *
     * @param digits a sequence of valid DTMF digits
     */
    public fun dtmf(digits: String)

    /**
     * Registers a [RemoteVideoTrackListener] for main video.
     *
     * @param listener a remote video track listener
     */
    public fun registerMainRemoteVideoTrackListener(listener: RemoteVideoTrackListener)

    /**
     * Unregisters a [RemoteVideoTrackListener] for main video.
     *
     * @param listener a remote video track listener
     */
    public fun unregisterMainRemoteVideoTrackListener(listener: RemoteVideoTrackListener)

    /**
     * Registers a [RemoteVideoTrackListener] for presentation video.
     *
     * @param listener a remote video track listener
     */
    public fun registerPresentationRemoteVideoTrackListener(listener: RemoteVideoTrackListener)

    /**
     * Unregisters a [RemoteVideoTrackListener] for presentation video.
     *
     * @param listener a remote video track listener
     */
    public fun unregisterPresentationRemoteVideoTrackListener(listener: RemoteVideoTrackListener)

    public fun interface RemoteVideoTrackListener {

        /**
         * Invoked when remote video track is added or removed.
         *
         * @param videoTrack an instance of video track or null
         */
        public fun onRemoteVideoTrack(videoTrack: VideoTrack?)
    }
}
