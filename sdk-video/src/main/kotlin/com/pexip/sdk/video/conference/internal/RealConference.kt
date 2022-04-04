package com.pexip.sdk.video.conference.internal

import com.pexip.sdk.media.MediaConnectionSignaling
import com.pexip.sdk.video.conference.Conference

internal class RealConference(
    private val refresher: TokenRefresher,
    private val signaling: MediaConnectionSignaling,
) : Conference, MediaConnectionSignaling by signaling {

    override fun leave() {
        refresher.shutdown()
    }
}
