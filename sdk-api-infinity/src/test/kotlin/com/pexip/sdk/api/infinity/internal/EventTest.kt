package com.pexip.sdk.api.infinity.internal

import com.pexip.sdk.api.infinity.ByeEvent
import com.pexip.sdk.api.infinity.DisconnectEvent
import com.pexip.sdk.api.infinity.Event
import com.pexip.sdk.api.infinity.PresentationStartEvent
import com.pexip.sdk.api.infinity.PresentationStopEvent
import com.pexip.sdk.api.infinity.nextString
import kotlinx.serialization.json.Json
import okio.FileSystem
import okio.Path.Companion.toPath
import java.util.UUID
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class EventTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `returns Event if the type is registered`() {
        val testCases = buildMap {
            this["bye"] = ByeEvent
            this["disconnect"] = DisconnectEvent("Disconnected by remote host")
            this["presentation_start"] = PresentationStartEvent(
                presenterId = UUID.fromString("0296f038-7f41-4c73-8dcf-0b95bd0138c7"),
                presenterName = "Dmitry"
            )
            this["presentation_stop"] = PresentationStopEvent
        }
        testCases.forEach { (type, event) ->
            assertEquals(
                expected = event,
                actual = Event(
                    json = json,
                    id = Random.nextString(8),
                    type = type,
                    data = FileSystem.RESOURCES.read("$type.json".toPath()) { readUtf8() }
                )
            )
        }
    }

    @Test
    fun `returns null if the type is not registered`() {
        assertNull(
            actual = Event(
                json = json,
                id = Random.nextString(8),
                type = Random.nextString(8),
                data = Random.nextString(8)
            )
        )
    }
}
