package com.pexip.sdk.conference.coroutines

import com.pexip.sdk.conference.Conference
import com.pexip.sdk.conference.ConferenceEvent
import com.pexip.sdk.conference.ConferenceEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

public fun Conference.getConferenceEvents(): Flow<ConferenceEvent> = callbackFlow {
    val listener = ConferenceEventListener(::trySend)
    registerConferenceEventListener(listener)
    awaitClose { unregisterConferenceEventListener(listener) }
}
