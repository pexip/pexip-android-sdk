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
package com.pexip.sdk.conference.infinity.internal

import com.pexip.sdk.api.infinity.InfinityService
import com.pexip.sdk.api.infinity.MessageRequest
import com.pexip.sdk.api.infinity.TokenStore
import com.pexip.sdk.conference.ConferenceEventListener
import com.pexip.sdk.conference.MessageReceivedConferenceEvent
import java.util.UUID

internal class RealMessenger(
    private val participantId: UUID,
    private val participantName: String,
    private val store: TokenStore,
    private val conferenceStep: InfinityService.ConferenceStep,
    private val listener: ConferenceEventListener,
    private val atProvider: () -> Long = System::currentTimeMillis,
) : Messenger {

    override fun message(payload: String) {
        val request = MessageRequest(
            payload = payload,
            type = "text/plain",
        )
        val token = store.get()
        val success = try {
            conferenceStep.message(request, token).execute()
        } catch (t: Throwable) {
            false
        }
        if (success) {
            val event = MessageReceivedConferenceEvent(
                at = atProvider(),
                participantId = participantId,
                participantName = participantName,
                type = request.type,
                payload = request.payload,
            )
            listener.onConferenceEvent(event)
        }
    }
}
