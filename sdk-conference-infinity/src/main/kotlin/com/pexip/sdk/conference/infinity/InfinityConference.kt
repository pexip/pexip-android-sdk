package com.pexip.sdk.conference.infinity

import com.pexip.sdk.api.infinity.InfinityService
import com.pexip.sdk.api.infinity.RequestTokenResponse
import com.pexip.sdk.conference.Conference
import com.pexip.sdk.conference.ConferenceEventListener
import com.pexip.sdk.conference.infinity.internal.ConferenceEventSource
import com.pexip.sdk.conference.infinity.internal.RealConferenceEventSource
import com.pexip.sdk.conference.infinity.internal.RealMediaConnectionSignaling
import com.pexip.sdk.conference.infinity.internal.RealTokenRefresher
import com.pexip.sdk.conference.infinity.internal.RealTokenStore
import com.pexip.sdk.conference.infinity.internal.TokenRefresher
import com.pexip.sdk.media.MediaConnectionSignaling
import java.net.URL
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService

public class InfinityConference private constructor(
    private val source: ConferenceEventSource,
    private val refresher: TokenRefresher,
    private val signaling: MediaConnectionSignaling,
    private val executor: ScheduledExecutorService,
) : Conference, MediaConnectionSignaling by signaling {

    override fun registerConferenceEventListener(listener: ConferenceEventListener) {
        source.registerConferenceEventListener(listener)
    }

    override fun unregisterConferenceEventListener(listener: ConferenceEventListener) {
        source.unregisterConferenceEventListener(listener)
    }

    override fun leave() {
        source.cancel()
        refresher.cancel()
        executor.shutdown()
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
            val executor = Executors.newSingleThreadScheduledExecutor()
            return InfinityConference(
                source = RealConferenceEventSource(store, conferenceStep, executor),
                refresher = RealTokenRefresher(response.expires, store, conferenceStep, executor),
                signaling = RealMediaConnectionSignaling(store, participantStep),
                executor = executor
            )
        }
    }
}
