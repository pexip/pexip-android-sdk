package com.pexip.sdk.conference.infinity

import com.pexip.sdk.api.infinity.InfinityService
import com.pexip.sdk.api.infinity.RequestTokenResponse
import com.pexip.sdk.api.infinity.TokenRefresher
import com.pexip.sdk.api.infinity.TokenStore
import com.pexip.sdk.conference.Conference
import com.pexip.sdk.conference.ConferenceEventListener
import com.pexip.sdk.conference.infinity.internal.ConferenceEventSource
import com.pexip.sdk.conference.infinity.internal.DtmfSender
import com.pexip.sdk.conference.infinity.internal.Messenger
import com.pexip.sdk.conference.infinity.internal.RealConferenceEventSource
import com.pexip.sdk.conference.infinity.internal.RealDtmfSender
import com.pexip.sdk.conference.infinity.internal.RealMediaConnectionSignaling
import com.pexip.sdk.conference.infinity.internal.RealMessenger
import com.pexip.sdk.conference.infinity.internal.maybeSubmit
import com.pexip.sdk.media.IceServer
import com.pexip.sdk.media.MediaConnectionSignaling
import java.net.URL
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService

public class InfinityConference private constructor(
    private val source: ConferenceEventSource,
    private val sender: DtmfSender,
    private val messenger: Messenger,
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

    override fun dtmf(digits: String) {
        executor.maybeSubmit {
            sender.send(digits)
        }
    }

    override fun message(payload: String) {
        executor.maybeSubmit {
            messenger.message(payload)
        }
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
            step = service.newRequest(node).conference(conferenceAlias),
            response = response
        )

        @JvmStatic
        public fun create(
            step: InfinityService.ConferenceStep,
            response: RequestTokenResponse,
        ): InfinityConference {
            val store = TokenStore.create(response)
            val participantStep = step.participant(response.participantId)
            val executor = Executors.newSingleThreadScheduledExecutor()
            val source = RealConferenceEventSource(
                store = store,
                conferenceStep = step,
                executor = executor
            )
            val iceServers = buildList(response.stun.size + response.turn.size) {
                val stunIceServers = response.stun.map {
                    IceServer.Builder(it.url).build()
                }
                addAll(stunIceServers)
                val turnIceServers = response.turn.map {
                    IceServer.Builder(it.urls)
                        .username(it.username)
                        .password(it.credential)
                        .build()
                }
                addAll(turnIceServers)
            }
            return InfinityConference(
                source = source,
                sender = RealDtmfSender(store, participantStep),
                messenger = RealMessenger(
                    participantId = response.participantId,
                    participantName = response.participantName,
                    store = store,
                    conferenceStep = step,
                    listener = source
                ),
                refresher = TokenRefresher.create(step, store, executor),
                signaling = RealMediaConnectionSignaling(
                    store = store,
                    participantStep = participantStep,
                    iceServers = iceServers
                ),
                executor = executor
            )
        }
    }
}
