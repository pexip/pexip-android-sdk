/*
 * Copyright 2022 Pexip AS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.pexip.sdk.media

/**
 * A media connection.
 *
 * @property mainRemoteVideoTrack current main remote video track or null
 * @property presentationRemoteVideoTrack current presentation remote video track or null
 */
public interface MediaConnection {

    public val mainRemoteVideoTrack: VideoTrack?

    public val presentationRemoteVideoTrack: VideoTrack?

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
     * @param localAudioTrack a local audio track to transmit
     */
    public fun setMainAudioTrack(localAudioTrack: LocalAudioTrack?)

    /**
     * Sets the main video track.
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
     * Enables or disables the receive of remote audio.
     *
     * @param enabled true if this [MediaConnection] should receive remote main audio, false otherwise
     */
    public fun setMainRemoteAudioTrackEnabled(enabled: Boolean)

    /**
     * Enables or disables the receive of remote video.
     *
     * @param enabled true if this [MediaConnection] should receive remote video audio, false otherwise
     */
    public fun setMainRemoteVideoTrackEnabled(enabled: Boolean)

    /**
     * Enables or disables the receive of ongoing remote presentation video.
     *
     * Doesn't have any effect if [MediaConnectionConfig.presentationInMain] is true.
     *
     * @param enabled true if this [MediaConnection] should receive remote presentation video, false otherwise
     */
    public fun setPresentationRemoteVideoTrackEnabled(enabled: Boolean)

    /**
     * Sets the maximum bitrate for each video stream.
     *
     * Passing an instance of [Bitrate] that is equal to zero bits per second will remove the
     * constraints and let the underlying media engine come up with the best value.
     *
     * By default, no maximum bitrate is set.
     *
     * @param bitrate a bitrate to set as maximum
     */
    public fun setMaxBitrate(bitrate: Bitrate)

    /**
     * Allows this [MediaConnection] to receive ongoing remote presentation.
     */
    @Deprecated(
        message = "Use setPresentationRemoteVideoTrackEnabled(true) instead.",
        replaceWith = ReplaceWith("setPresentationRemoteVideoTrackEnabled(true)"),
    )
    public fun startPresentationReceive()

    /**
     * Disables the ability to receive ongoing remote presentation.
     */
    @Deprecated(
        message = "Use setPresentationRemoteVideoTrackEnabled(false) instead.",
        replaceWith = ReplaceWith("setPresentationRemoteVideoTrackEnabled(false)"),
    )
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
