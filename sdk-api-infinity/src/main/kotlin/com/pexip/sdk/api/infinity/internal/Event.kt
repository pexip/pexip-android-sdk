package com.pexip.sdk.api.infinity.internal

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

internal sealed interface Event {

    companion object {

        private val json by lazy { Json { ignoreUnknownKeys = true } }

        fun from(id: String?, type: String?, data: String) = when (type) {
            "presentation_start" -> json.decodeFromString<PresentationStartEvent>(data)
            "presentation_stop" -> PresentationStopEvent
            "disconnect" -> json.decodeFromString<DisconnectEvent>(data)
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
