package com.pexip.sdk.conference.infinity

import com.pexip.sdk.api.infinity.InfinityService
import com.pexip.sdk.api.infinity.RequestTokenResponse
import com.pexip.sdk.conference.Conference
import com.pexip.sdk.conference.infinity.internal.RealMediaConnectionSignaling
import com.pexip.sdk.conference.infinity.internal.RealTokenRefresher
import com.pexip.sdk.conference.infinity.internal.RealTokenStore
import com.pexip.sdk.conference.infinity.internal.TokenRefresher
import com.pexip.sdk.media.MediaConnectionSignaling
import java.net.URL

public class InfinityConference private constructor(
    private val refresher: TokenRefresher,
    private val signaling: MediaConnectionSignaling,
) : Conference, MediaConnectionSignaling by signaling {

    override fun leave() {
        refresher.dispose()
    }

    public companion object {

        @JvmStatic
        public fun create(
            service: InfinityService,
            node: URL,
            conferenceAlias: String,
            response: RequestTokenResponse,
        ): InfinityConference = create(
            conferenceStep = service.newRequest(node).conference(conferenceAlias),
            response = response
        )

        @JvmStatic
        public fun create(
            conferenceStep: InfinityService.ConferenceStep,
            response: RequestTokenResponse,
        ): InfinityConference {
            val store = RealTokenStore(response.token)
            val participantStep = conferenceStep.participant(response.participantId)
            return InfinityConference(
                refresher = RealTokenRefresher(response.expires, store, conferenceStep),
                signaling = RealMediaConnectionSignaling(store, participantStep)
            )
        }
    }
}
