package com.pexip.sdk.video.conference

import com.pexip.sdk.api.infinity.InfinityService
import com.pexip.sdk.api.infinity.RequestTokenResponse
import com.pexip.sdk.media.MediaConnectionSignaling
import com.pexip.sdk.video.conference.internal.RealConference
import com.pexip.sdk.video.conference.internal.RealMediaConnectionSignaling
import com.pexip.sdk.video.conference.internal.RealTokenRefresher
import com.pexip.sdk.video.conference.internal.RealTokenStore
import java.net.URL

/**
 * Represents a conference.
 */
public interface Conference : MediaConnectionSignaling {

    /**
     * Leaves the conference. Once left, the [Conference] object is no longer valid.
     */
    public fun leave()

    public companion object {

        @JvmStatic
        public fun create(
            service: InfinityService,
            node: URL,
            conferenceAlias: String,
            response: RequestTokenResponse,
        ): Conference {
            val store = RealTokenStore(response.token)
            val conferenceStep = service.newRequest(node).conference(conferenceAlias)
            val participantStep = conferenceStep.participant(response.participantId)
            return RealConference(
                refresher = RealTokenRefresher(response.expires, store, conferenceStep),
                signaling = RealMediaConnectionSignaling(store, participantStep)
            )
        }
    }
}
