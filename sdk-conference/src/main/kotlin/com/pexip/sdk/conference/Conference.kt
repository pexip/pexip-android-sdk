package com.pexip.sdk.conference

import com.pexip.sdk.media.MediaConnectionSignaling

/**
 * Represents a conference.
 */
public interface Conference : MediaConnectionSignaling {

    public fun registerConferenceEventListener(listener: ConferenceEventListener)

    public fun unregisterConferenceEventListener(listener: ConferenceEventListener)

    public fun message(payload: String)

    /**
     * Leaves the conference. Once left, the [Conference] object is no longer valid.
     */
    public fun leave()
}
