/*
 * Copyright 2022-2024 Pexip AS
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
package com.pexip.sdk.conference.infinity

import com.pexip.sdk.api.coroutines.await
import com.pexip.sdk.api.infinity.InfinityService
import com.pexip.sdk.api.infinity.RequestTokenResponse
import com.pexip.sdk.api.infinity.TokenStore
import com.pexip.sdk.api.infinity.TokenStore.Companion.refreshTokenIn
import com.pexip.sdk.conference.Conference
import com.pexip.sdk.conference.ConferenceEvent
import com.pexip.sdk.conference.ConferenceEventListener
import com.pexip.sdk.conference.Messenger
import com.pexip.sdk.conference.Referer
import com.pexip.sdk.conference.Roster
import com.pexip.sdk.conference.Theme
import com.pexip.sdk.conference.infinity.internal.ConferenceEvent
import com.pexip.sdk.conference.infinity.internal.DataChannelImpl
import com.pexip.sdk.conference.infinity.internal.DataChannelMessengerImpl
import com.pexip.sdk.conference.infinity.internal.MediaConnectionSignalingImpl
import com.pexip.sdk.conference.infinity.internal.MessengerImpl
import com.pexip.sdk.conference.infinity.internal.RefererImpl
import com.pexip.sdk.conference.infinity.internal.RosterImpl
import com.pexip.sdk.conference.infinity.internal.ThemeImpl
import com.pexip.sdk.conference.infinity.internal.WhileSubscribedAtLeast
import com.pexip.sdk.conference.infinity.internal.events
import com.pexip.sdk.core.retry
import com.pexip.sdk.infinity.UnsupportedInfinityException
import com.pexip.sdk.media.IceServer
import com.pexip.sdk.media.MediaConnectionSignaling
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import java.net.URL
import java.util.concurrent.CopyOnWriteArraySet
import java.util.concurrent.Executors

public class InfinityConference private constructor(
    private val step: InfinityService.ConferenceStep,
    response: RequestTokenResponse,
) : Conference {

    private val executor = Executors.newSingleThreadScheduledExecutor()
    private val scope = CoroutineScope(SupervisorJob() + executor.asCoroutineDispatcher())
    private val store = TokenStore.create(response)

    // To ensure that all subscribers see SSE we wait until all known subscribers are active.
    // This has to be updated every time we add another subscriber to the event.
    private val threshold = if (response.dataChannelId == -1) 5 else 6
    private val event =
        step.events(store).shareIn(scope, SharingStarted.WhileSubscribedAtLeast(threshold))
    private val listeners = CopyOnWriteArraySet<ConferenceEventListener>()
    private val mutableConferenceEvent = MutableSharedFlow<ConferenceEvent>()

    override val name: String = response.conferenceName

    override val theme: Theme = ThemeImpl(
        scope = scope,
        event = event,
        step = step,
        store = store,
    )

    override val roster: Roster = RosterImpl(
        scope = scope,
        event = event,
    )

    override val referer: Referer = RefererImpl(step.requestBuilder, response.directMediaRequested)

    override val signaling: MediaConnectionSignaling = MediaConnectionSignalingImpl(
        scope = scope,
        event = event,
        store = store,
        participantStep = step.participant(response.participantId),
        directMedia = response.directMedia,
        iceServers = buildList(response.stun.size + response.turn.size) {
            this += response.stun.map { IceServer.Builder(it.url).build() }
            this += response.turn.map {
                IceServer.Builder(it.urls)
                    .username(it.username)
                    .password(it.credential)
                    .build()
            }
        },
        iceTransportsRelayOnly = response.useRelayCandidatesOnly,
        dataChannel = when (val id = response.dataChannelId) {
            -1 -> null
            else -> DataChannelImpl(id)
        },
    )

    override val messenger: Messenger = when (val dataChannel = signaling.dataChannel) {
        null -> MessengerImpl(
            scope = scope,
            event = event,
            senderId = response.participantId,
            senderName = response.participantName,
            store = store,
            step = step,
        )
        else -> DataChannelMessengerImpl(
            scope = scope,
            senderId = response.participantId,
            senderName = response.participantName,
            dataChannel = dataChannel,
        )
    }

    init {
        store.refreshTokenIn(
            scope = scope,
            refreshToken = { retry { step.refreshToken(it).await() } },
            releaseToken = { retry { step.releaseToken(it).await() } },
            onFailure = { mutableConferenceEvent.emit(ConferenceEvent(it)) },
        )
        merge(event.mapNotNull(::ConferenceEvent), mutableConferenceEvent)
            .onEach { event -> listeners.forEach { it.onConferenceEvent(event) } }
            .flowOn(Dispatchers.Main.immediate)
            .launchIn(scope)
    }

    override fun registerConferenceEventListener(listener: ConferenceEventListener) {
        listeners += listener
    }

    override fun unregisterConferenceEventListener(listener: ConferenceEventListener) {
        listeners -= listener
    }

    override fun leave() {
        scope.cancel()
        executor.shutdown()
        listeners.clear()
    }

    public companion object {

        /**
         * Creates a new instance of [InfinityService].
         *
         * @param service an instance of [InfinityConference]
         * @param node a URL of the node
         * @param conferenceAlias a conference alias
         * @param response a request registration token response
         * @return an instance of [InfinityConference]
         * @throws UnsupportedInfinityException if the version of Infinity is not supported
         */
        @JvmStatic
        public fun create(
            service: InfinityService,
            node: URL,
            conferenceAlias: String,
            response: RequestTokenResponse,
        ): InfinityConference = create(
            step = service.newRequest(node).conference(conferenceAlias),
            response = response,
        )

        /**
         * Creates a new instance of [InfinityConference].
         *
         * @param step a conference step
         * @param response a request conference token response
         * @return an instance of [InfinityConference]
         * @throws UnsupportedInfinityException if the version of Infinity is not supported
         */
        @JvmStatic
        public fun create(
            step: InfinityService.ConferenceStep,
            response: RequestTokenResponse,
        ): InfinityConference {
            if (response.version.versionId < "29") {
                throw UnsupportedInfinityException(response.version.versionId)
            }
            return InfinityConference(step, response)
        }
    }
}
