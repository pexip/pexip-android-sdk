/*
 * Copyright 2022-2023 Pexip AS
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

import com.pexip.sdk.api.infinity.InfinityService
import com.pexip.sdk.api.infinity.RequestTokenResponse
import com.pexip.sdk.api.infinity.TokenRefresher
import com.pexip.sdk.api.infinity.TokenStore
import com.pexip.sdk.conference.Conference
import com.pexip.sdk.conference.ConferenceEventListener
import com.pexip.sdk.conference.Message
import com.pexip.sdk.conference.MessageNotSentException
import com.pexip.sdk.conference.MessageReceivedConferenceEvent
import com.pexip.sdk.conference.Messenger
import com.pexip.sdk.conference.SendCallback
import com.pexip.sdk.conference.infinity.internal.ConferenceEvent
import com.pexip.sdk.conference.infinity.internal.ConferenceEventSource
import com.pexip.sdk.conference.infinity.internal.MessengerImpl
import com.pexip.sdk.conference.infinity.internal.RealConferenceEventSource
import com.pexip.sdk.conference.infinity.internal.RealMediaConnectionSignaling
import com.pexip.sdk.conference.infinity.internal.maybeSubmit
import com.pexip.sdk.media.IceServer
import com.pexip.sdk.media.MediaConnectionSignaling
import java.net.URL
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService

public class InfinityConference private constructor(
    override val name: String,
    override val messenger: Messenger,
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

    @Deprecated(
        message = "Use MediaConnection.dtmf() instead.",
        level = DeprecationLevel.ERROR,
    )
    override fun dtmf(digits: String) {
    }

    @Deprecated("Use Conference.messenger.send() instead.")
    override fun message(payload: String) {
        messenger.send(
            type = "text/plain",
            payload = payload,
            callback = object : SendCallback {

                override fun onSuccess(message: Message) {
                    executor.maybeSubmit {
                        val event = MessageReceivedConferenceEvent(
                            at = message.at,
                            participantId = message.participantId,
                            participantName = message.participantName,
                            type = message.type,
                            payload = message.payload,
                        )
                        source.onConferenceEvent(event)
                    }
                }

                override fun onFailure(e: MessageNotSentException) {
                    // noop
                }
            },
        )
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
            response = response,
        )

        @JvmStatic
        public fun create(
            step: InfinityService.ConferenceStep,
            response: RequestTokenResponse,
        ): InfinityConference {
            val store = TokenStore.create(response)
            val participantStep = step.participant(response.participantId)
            val executor = Executors.newSingleThreadScheduledExecutor()
            val messenger = MessengerImpl(
                senderId = response.participantId,
                senderName = response.participantName,
                store = store,
                step = step,
            )
            val source = RealConferenceEventSource(store, step, executor, messenger)
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
                name = response.conferenceName,
                source = source,
                messenger = messenger,
                refresher = TokenRefresher.create(step, store, executor) {
                    source.onConferenceEvent(ConferenceEvent(it))
                },
                signaling = RealMediaConnectionSignaling(
                    store = store,
                    participantStep = participantStep,
                    iceServers = iceServers,
                ),
                executor = executor,
            )
        }
    }
}
