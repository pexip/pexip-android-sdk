package com.pexip.sdk.video.internal

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString

internal sealed interface Event {

    companion object {

        fun from(id: String?, type: String?, data: String) = when (type) {
            "presentation_start" -> Json.decodeFromString<PresentationStartEvent>(data)
            "presentation_stop" -> PresentationStopEvent
            "disconnect" -> Json.decodeFromString<DisconnectEvent>(data)
            "bye" -> ByeEvent
            else -> UnknownEvent(id, type, data)
        }
    }
}

@Serializable
internal data class PresentationStartEvent(val presenter_uuid: String) : Event

internal object PresentationStopEvent : Event {

    override fun toString(): String = "PresentationStopEvent"
}

@Serializable
internal data class DisconnectEvent(val reason: String) : Event

internal object ByeEvent : Event {

    override fun toString(): String = "ByeEvent"
}

internal data class UnknownEvent(val id: String?, val type: String?, val data: String) : Event
