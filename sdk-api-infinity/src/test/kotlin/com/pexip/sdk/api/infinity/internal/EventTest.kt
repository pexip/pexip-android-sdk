package com.pexip.sdk.api.infinity.internal

import com.pexip.sdk.api.Event
import com.pexip.sdk.api.infinity.ByeEvent
import com.pexip.sdk.api.infinity.DisconnectEvent
import com.pexip.sdk.api.infinity.Event
import com.pexip.sdk.api.infinity.FeccAction
import com.pexip.sdk.api.infinity.FeccEvent
import com.pexip.sdk.api.infinity.FeccMovement
import com.pexip.sdk.api.infinity.IncomingCancelledEvent
import com.pexip.sdk.api.infinity.IncomingEvent
import com.pexip.sdk.api.infinity.InfinityService
import com.pexip.sdk.api.infinity.MessageReceivedEvent
import com.pexip.sdk.api.infinity.PresentationStartEvent
import com.pexip.sdk.api.infinity.PresentationStopEvent
import com.pexip.sdk.api.infinity.nextString
import okio.FileSystem
import okio.Path.Companion.toPath
import java.util.UUID
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class EventTest {

    @Test
    fun `returns Event if the type is registered`() {
        val testCases = listOf(
            TestCase(type = "bye", event = ByeEvent),
            TestCase(type = "disconnect", event = DisconnectEvent("Disconnected by remote host")),
            TestCase(
                type = "message_received",
                event = MessageReceivedEvent(
                    participantName = "George",
                    participantId = UUID.fromString("dc46269f-5b39-4356-93fd-94d31b890bd5"),
                    type = "text/plain",
                    payload = "hello"
                )
            ),
            TestCase(
                type = "presentation_start",
                event = PresentationStartEvent(
                    presenterId = UUID.fromString("0296f038-7f41-4c73-8dcf-0b95bd0138c7"),
                    presenterName = "George"
                )
            ),
            TestCase(type = "presentation_stop", event = PresentationStopEvent),
            TestCase(
                type = "incoming",
                event = IncomingEvent(
                    conferenceAlias = "george@example.com",
                    remoteDisplayName = "George",
                    token = "CXU3tuRZbF673lPdVbg9p3ZtOv7iuTOO0BSo2yFF1U9_qxKdlQAMr2wNZBwW1xccPMgFEI_MjF9SpRzu6nxi5zwaXdOsQbblemYYOn8pShCT1bn1QIWx0RC0H-L4heWaGQXY1dpIByDInVK5vLu88Uv0cb_dbxzhrlaIfm9_WP9YLmCVsvmFOhmDKx0bZxRTOP_yziFjl5xNxWAJQ8NL3assEFIfptXGN89Fp6jomreIOSktfVlSnMJe1OG6fqeiKYQS4WP-ie2d3nQ1tVFfxzYeU0fGHh4Wvxqozmbqs_zjpg=="
                )
            ),
            TestCase(
                type = "incoming_cancelled",
                event = IncomingCancelledEvent("CXU3tuRZbF673lPdVbg9p3ZtOv7iuTOO0BSo2yFF1U9_qxKdlQAMr2wNZBwW1xccPMgFEI_MjF9SpRzu6nxi5zwaXdOsQbblemYYOn8pShCT1bn1QIWx0RC0H-L4heWaGQXY1dpIByDInVK5vLu88Uv0cb_dbxzhrlaIfm9_WP9YLmCVsvmFOhmDKx0bZxRTOP_yziFjl5xNxWAJQ8NL3assEFIfptXGN89Fp6jomreIOSktfVlSnMJe1OG6fqeiKYQS4WP-ie2d3nQ1tVFfxzYeU0fGHh4Wvxqozmbqs_zjpg==")
            ),
            TestCase(
                type = "fecc",
                event = FeccEvent(
                    action = FeccAction.START,
                    timeout = 1000,
                    movement = listOf(FeccMovement.PAN_LEFT, FeccMovement.TILT_UP)
                ),
                filename = "fecc_start.json"
            ),
            TestCase(
                type = "fecc",
                event = FeccEvent(
                    action = FeccAction.CONTINUE,
                    timeout = 200,
                    movement = listOf(FeccMovement.ZOOM_IN, FeccMovement.PAN_RIGHT)
                ),
                filename = "fecc_continue.json"
            ),
            TestCase(
                type = "fecc",
                event = FeccEvent(
                    action = FeccAction.STOP,
                    timeout = 100,
                    movement = listOf(FeccMovement.TILT_DOWN, FeccMovement.ZOOM_OUT)
                ),
                filename = "fecc_stop.json"
            ),
            TestCase(
                type = "fecc",
                event = FeccEvent(
                    action = FeccAction.UNKNOWN,
                    timeout = 0,
                    movement = List(3) { FeccMovement.UNKNOWN }
                ),
                filename = "fecc_unknown.json"
            )
        )
        testCases.forEach { (type, event, filename) ->
            assertEquals(
                expected = event,
                actual = Event(
                    json = InfinityService.Json,
                    id = Random.nextString(8),
                    type = type,
                    data = FileSystem.RESOURCES.read(filename.toPath()) { readUtf8() }
                )
            )
        }
    }

    @Test
    fun `returns null if the type is not registered`() {
        assertNull(
            actual = Event(
                json = InfinityService.Json,
                id = Random.nextString(8),
                type = Random.nextString(8),
                data = Random.nextString(8)
            )
        )
    }

    private data class TestCase(
        val type: String,
        val event: Event,
        val filename: String = "$type.json",
    )
}
