package com.pexip.sdk.video.conference.internal

import okio.FileSystem
import okio.Path.Companion.toPath
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

internal class EventTest {

    @Test
    fun `returns Event if the type is registered`() {
        val testCases = buildMap {
            this["bye"] = ByeEvent
            this["disconnect"] = DisconnectEvent("Disconnected by remote host")
            this["presentation_start"] =
                PresentationStartEvent("0296f038-7f41-4c73-8dcf-0b95bd0138c7")
            this["presentation_stop"] = PresentationStopEvent
        }
        testCases.forEach { (type, event) ->
            assertEquals(
                expected = event,
                actual = Event.from(
                    id = "${Random.nextInt()}",
                    type = type,
                    data = FileSystem.RESOURCES.read("$type.json".toPath()) { readUtf8() }
                )
            )
        }
    }

    @Test
    fun `returns UnknownEvent if the type is not registered`() {
        val event = UnknownEvent(
            id = "${Random.nextInt()}",
            type = "${Random.nextInt()}",
            data = "${Random.nextInt()}"
        )
        assertEquals(
            expected = event,
            actual = Event.from(
                id = event.id,
                type = event.type,
                data = event.data
            )
        )
    }
}
