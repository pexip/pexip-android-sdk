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
import com.pexip.sdk.conference.MessageNotSentException
import com.pexip.sdk.conference.MessageReceivedConferenceEvent
import com.pexip.sdk.conference.Messenger
import com.pexip.sdk.conference.coroutines.send
import com.pexip.sdk.conference.infinity.internal.ConferenceEvent
import com.pexip.sdk.conference.infinity.internal.MessengerImpl
import com.pexip.sdk.conference.infinity.internal.RealConferenceEventSource
import com.pexip.sdk.conference.infinity.internal.RealMediaConnectionSignaling
import com.pexip.sdk.media.IceServer
import com.pexip.sdk.media.MediaConnectionSignaling
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.net.URL
import java.util.concurrent.Executors
import java.util.logging.Logger

public class InfinityConference private constructor(
    step: InfinityService.ConferenceStep,
    response: RequestTokenResponse,
) : Conference {

    private val executor = Executors.newSingleThreadScheduledExecutor()
    private val scope = CoroutineScope(SupervisorJob() + executor.asCoroutineDispatcher())
    private val store = TokenStore.create(response)
    private val refresher = TokenRefresher.create(step, store, executor) {
        source.onConferenceEvent(ConferenceEvent(it))
    }
    private val messengerImpl = MessengerImpl(
        senderId = response.participantId,
        senderName = response.participantName,
        store = store,
        step = step,
    )
    private val source = RealConferenceEventSource(store, step, executor, messengerImpl)

    override val name: String = response.conferenceName

    override val messenger: Messenger = messengerImpl

    override val signaling: MediaConnectionSignaling = RealMediaConnectionSignaling(
        store = store,
        participantStep = step.participant(response.participantId),
        iceServers = buildList(response.stun.size + response.turn.size) {
            this += response.stun.map { IceServer.Builder(it.url).build() }
            this += response.turn.map {
                IceServer.Builder(it.urls)
                    .username(it.username)
                    .password(it.credential)
                    .build()
            }
        },
    )

    override fun registerConferenceEventListener(listener: ConferenceEventListener) {
        source.registerConferenceEventListener(listener)
    }

    override fun unregisterConferenceEventListener(listener: ConferenceEventListener) {
        source.unregisterConferenceEventListener(listener)
    }

    @Deprecated("Use Conference.messenger.send() instead.")
    override fun message(payload: String) {
        scope.launch {
            val message = try {
                messenger.send(type = "text/plain", payload = payload)
            } catch (e: MessageNotSentException) {
                return@launch
            }
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

    override fun leave() {
        scope.cancel()
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
            if (response.version.versionId < "29") {
                val logger = Logger.getLogger("InfinityConference")
                val msg = buildString {
                    append("Infinity ")
                    append(response.version.versionId)
                    append(" is not officially supported by the SDK.")
                    append(" Please upgrade your Infinity deployment to 29 or newer.")
                }
                logger.warning(msg)
            }
            return InfinityConference(step, response)
        }
    }
}
