package com.pexip.sdk.conference

import com.pexip.sdk.media.MediaConnectionSignaling

/**
 * Represents a conference.
 *
 * @property name a display name of this [Conference]
 */
public interface Conference : MediaConnectionSignaling {

    public val name: String

    /**
     * Registers a [ConferenceEventListener].
     *
     * @param listener a conference event listener
     */
    public fun registerConferenceEventListener(listener: ConferenceEventListener)

    /**
     * Unregisters a [ConferenceEventListener].
     *
     * @param listener a conference event listener
     */
    public fun unregisterConferenceEventListener(listener: ConferenceEventListener)

    /**
     * Sends DTMF digits to this [Conference].
     *
     * @param digits a sequence of valid DTMF digits
     */
    @Deprecated(
        message = "Use MediaConnection.dtmf() instead.",
        level = DeprecationLevel.ERROR
    )
    public fun dtmf(digits: String)

    /**
     * Sends a plain text message to this [Conference].
     *
     * @param payload a plain text message
     */
    public fun message(payload: String)

    /**
     * Leaves the conference. Once left, the [Conference] object is no longer valid.
     */
    public fun leave()
}
