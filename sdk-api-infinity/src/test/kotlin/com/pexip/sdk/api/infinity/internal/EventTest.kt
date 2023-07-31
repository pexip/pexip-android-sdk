/*
 * Copyright 2022-2023 Pexip AS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
import com.pexip.sdk.api.infinity.ReferEvent
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
                    payload = "hello",
                    direct = true,
                ),
            ),
            TestCase(
                type = "presentation_start",
                event = PresentationStartEvent(
                    presenterId = UUID.fromString("0296f038-7f41-4c73-8dcf-0b95bd0138c7"),
                    presenterName = "George",
                ),
            ),
            TestCase(type = "presentation_stop", event = PresentationStopEvent),
            TestCase(
                type = "incoming",
                event = IncomingEvent(
                    conferenceAlias = "george@example.com",
                    remoteDisplayName = "George",
                    token = "CXU3tuRZbF673lPdVbg9p3ZtOv7iuTOO0BSo2yFF1U9_qxKdlQAMr2wNZBwW1xccPMgFEI_MjF9SpRzu6nxi5zwaXdOsQbblemYYOn8pShCT1bn1QIWx0RC0H-L4heWaGQXY1dpIByDInVK5vLu88Uv0cb_dbxzhrlaIfm9_WP9YLmCVsvmFOhmDKx0bZxRTOP_yziFjl5xNxWAJQ8NL3assEFIfptXGN89Fp6jomreIOSktfVlSnMJe1OG6fqeiKYQS4WP-ie2d3nQ1tVFfxzYeU0fGHh4Wvxqozmbqs_zjpg==",
                ),
            ),
            TestCase(
                type = "incoming_cancelled",
                event = IncomingCancelledEvent("CXU3tuRZbF673lPdVbg9p3ZtOv7iuTOO0BSo2yFF1U9_qxKdlQAMr2wNZBwW1xccPMgFEI_MjF9SpRzu6nxi5zwaXdOsQbblemYYOn8pShCT1bn1QIWx0RC0H-L4heWaGQXY1dpIByDInVK5vLu88Uv0cb_dbxzhrlaIfm9_WP9YLmCVsvmFOhmDKx0bZxRTOP_yziFjl5xNxWAJQ8NL3assEFIfptXGN89Fp6jomreIOSktfVlSnMJe1OG6fqeiKYQS4WP-ie2d3nQ1tVFfxzYeU0fGHh4Wvxqozmbqs_zjpg=="),
            ),
            TestCase(
                type = "fecc",
                event = FeccEvent(
                    action = FeccAction.START,
                    timeout = 1000,
                    movement = listOf(FeccMovement.PAN_LEFT, FeccMovement.TILT_UP),
                ),
                filename = "fecc_start.json",
            ),
            TestCase(
                type = "fecc",
                event = FeccEvent(
                    action = FeccAction.CONTINUE,
                    timeout = 200,
                    movement = listOf(FeccMovement.ZOOM_IN, FeccMovement.PAN_RIGHT),
                ),
                filename = "fecc_continue.json",
            ),
            TestCase(
                type = "fecc",
                event = FeccEvent(
                    action = FeccAction.STOP,
                    timeout = 100,
                    movement = listOf(FeccMovement.TILT_DOWN, FeccMovement.ZOOM_OUT),
                ),
                filename = "fecc_stop.json",
            ),
            TestCase(
                type = "fecc",
                event = FeccEvent(
                    action = FeccAction.UNKNOWN,
                    timeout = 0,
                    movement = List(3) { FeccMovement.UNKNOWN },
                ),
                filename = "fecc_unknown.json",
            ),
            TestCase(
                type = "refer",
                event = ReferEvent(
                    conferenceAlias = "toto",
                    token = "ZqWyw87Yr03g-vH_VCqZBTMemTcmcwwUrHIpq9LWl8Kn8DGc1yBmeMSN-ux5KRsO70QRtOvLfyoasEeioIve4wsUgCAsi6y_GUqc2Af40TcCHRm3RF5fEUqPo0x8P32Nc3BhmaTk5Mz2YP8t8v5YCggcaHDU1d_ddWZszWUwa_sszv-9h3FxmpTzT-zuB67RXfdBQlStbt86paf5S-6E9kzB2QJCKfrB1U9-juF-czmMibaEODEVC88V2Rlf8GIer2w=",
                ),
            ),
        )
        testCases.forEach { (type, event, filename) ->
            assertEquals(
                expected = event,
                actual = Event(
                    json = InfinityService.Json,
                    id = Random.nextString(8),
                    type = type,
                    data = FileSystem.RESOURCES.read(filename.toPath()) { readUtf8() },
                ),
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
                data = Random.nextString(8),
            ),
        )
    }

    private data class TestCase(
        val type: String,
        val event: Event,
        val filename: String = "$type.json",
    )
}
