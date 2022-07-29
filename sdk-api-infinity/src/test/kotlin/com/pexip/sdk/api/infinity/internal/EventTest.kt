package com.pexip.sdk.api.infinity.internal

import com.pexip.sdk.api.infinity.ByeEvent
import com.pexip.sdk.api.infinity.DisconnectEvent
import com.pexip.sdk.api.infinity.Event
import com.pexip.sdk.api.infinity.IncomingCancelledEvent
import com.pexip.sdk.api.infinity.IncomingEvent
import com.pexip.sdk.api.infinity.MessageReceivedEvent
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
            this["message_received"] = MessageReceivedEvent(
                participantName = "George",
                participantId = UUID.fromString("dc46269f-5b39-4356-93fd-94d31b890bd5"),
                type = "text/plain",
                payload = "hello"
            )
            this["presentation_start"] = PresentationStartEvent(
                presenterId = UUID.fromString("0296f038-7f41-4c73-8dcf-0b95bd0138c7"),
                presenterName = "George"
            )
            this["presentation_stop"] = PresentationStopEvent
            val token =
                "CXU3tuRZbF673lPdVbg9p3ZtOv7iuTOO0BSo2yFF1U9_qxKdlQAMr2wNZBwW1xccPMgFEI_MjF9SpRzu6nxi5zwaXdOsQbblemYYOn8pShCT1bn1QIWx0RC0H-L4heWaGQXY1dpIByDInVK5vLu88Uv0cb_dbxzhrlaIfm9_WP9YLmCVsvmFOhmDKx0bZxRTOP_yziFjl5xNxWAJQ8NL3assEFIfptXGN89Fp6jomreIOSktfVlSnMJe1OG6fqeiKYQS4WP-ie2d3nQ1tVFfxzYeU0fGHh4Wvxqozmbqs_zjpg=="
            this["incoming"] = IncomingEvent(
                conferenceAlias = "george@example.com",
                remoteDisplayName = "George",
                token = token
            )
            this["incoming_cancelled"] = IncomingCancelledEvent(token)
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
