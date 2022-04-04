package com.pexip.sdk.conference

import com.pexip.sdk.media.MediaConnectionSignaling

/**
 * Represents a conference.
 */
public interface Conference : MediaConnectionSignaling {

    /**
     * Leaves the conference. Once left, the [Conference] object is no longer valid.
     */
    public fun leave()
}
