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

import com.pexip.sdk.api.Event
import com.pexip.sdk.api.coroutines.asFlow
import com.pexip.sdk.api.infinity.DisconnectEvent
import com.pexip.sdk.api.infinity.InfinityService
import com.pexip.sdk.api.infinity.MessageReceivedEvent
import com.pexip.sdk.api.infinity.PresentationStartEvent
import com.pexip.sdk.api.infinity.PresentationStopEvent
import com.pexip.sdk.api.infinity.ReferEvent
import com.pexip.sdk.api.infinity.TokenStore
import com.pexip.sdk.conference.DisconnectConferenceEvent
import com.pexip.sdk.conference.FailureConferenceEvent
import com.pexip.sdk.conference.MessageReceivedConferenceEvent
import com.pexip.sdk.conference.PresentationStartConferenceEvent
import com.pexip.sdk.conference.PresentationStopConferenceEvent
import com.pexip.sdk.conference.ReferConferenceEvent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.retryWhen
import kotlin.time.Duration.Companion.seconds

internal inline fun ConferenceEvent(
    event: Event,
    at: () -> Long = System::currentTimeMillis,
) = when (event) {
    is PresentationStartEvent -> PresentationStartConferenceEvent(
        at = at(),
        presenterId = event.presenterId,
        presenterName = event.presenterName,
    )
    is PresentationStopEvent -> PresentationStopConferenceEvent(at())
    is MessageReceivedEvent -> MessageReceivedConferenceEvent(
        at = at(),
        participantId = event.participantId,
        participantName = event.participantName,
        type = event.type,
        payload = event.payload,
    )
    is ReferEvent -> ReferConferenceEvent(
        at = at(),
        conferenceAlias = event.conferenceAlias,
        token = event.token,
    )
    is DisconnectEvent -> DisconnectConferenceEvent(
        at = at(),
        reason = event.reason,
    )
    else -> null
}

internal inline fun ConferenceEvent(t: Throwable, at: () -> Long = System::currentTimeMillis) =
    FailureConferenceEvent(at(), t)

@OptIn(ExperimentalCoroutinesApi::class)
internal fun InfinityService.ConferenceStep.conferenceEvent(
    store: TokenStore,
    at: () -> Long = System::currentTimeMillis,
) = flow { emit(store.get()) }
    .flatMapLatest { events(it).asFlow() }
    .mapNotNull { ConferenceEvent(it, at) }
    .retryWhen { _, attempt ->
        delay(attempt.seconds.coerceAtMost(5.seconds))
        true
    }
