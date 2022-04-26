package com.pexip.sdk.conference.infinity.internal

import com.pexip.sdk.api.infinity.InfinityService
import com.pexip.sdk.api.infinity.MessageRequest
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
        val request = MessageRequest(payload)
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
                payload = request.payload
            )
            listener.onConferenceEvent(event)
        }
    }
}
