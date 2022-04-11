package com.pexip.sdk.api.infinity

import com.pexip.sdk.api.Event
import com.pexip.sdk.api.infinity.internal.UUIDSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.util.UUID

@Serializable
public data class PresentationStartEvent(
    @SerialName("presenter_name")
    val presenterName: String,
    @Serializable(with = UUIDSerializer::class)
    @SerialName("presenter_uuid")
    val presenterId: UUID,
) : Event

public object PresentationStopEvent : Event {

    override fun toString(): String = "PresentationStopEvent"
}

@Serializable
public data class DisconnectEvent(val reason: String) : Event

public object ByeEvent : Event {

    override fun toString(): String = "ByeEvent"
}

internal fun Event(json: Json, id: String?, type: String?, data: String) = when (type) {
    "presentation_start" -> json.decodeFromString<PresentationStartEvent>(data)
    "presentation_stop" -> PresentationStopEvent
    "disconnect" -> json.decodeFromString<DisconnectEvent>(data)
    "bye" -> ByeEvent
    else -> null
}
