package com.pexip.sdk.conference.infinity.internal

import com.pexip.sdk.conference.ConferenceEventListener

internal interface ConferenceEventSource : ConferenceEventListener {

    fun registerConferenceEventListener(listener: ConferenceEventListener)

    fun unregisterConferenceEventListener(listener: ConferenceEventListener)

    fun cancel()
}
