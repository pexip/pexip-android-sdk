/*
 * Copyright 2022-2024 Pexip AS
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

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.tableOf
import com.pexip.sdk.api.Event
import com.pexip.sdk.api.infinity.BreakoutBeginEvent
import com.pexip.sdk.api.infinity.BreakoutEndEvent
import com.pexip.sdk.api.infinity.BreakoutEvent
import com.pexip.sdk.api.infinity.ByeEvent
import com.pexip.sdk.api.infinity.ConferenceUpdateEvent
import com.pexip.sdk.api.infinity.DisconnectEvent
import com.pexip.sdk.api.infinity.FeccAction
import com.pexip.sdk.api.infinity.FeccEvent
import com.pexip.sdk.api.infinity.FeccMovement
import com.pexip.sdk.api.infinity.IncomingCancelledEvent
import com.pexip.sdk.api.infinity.IncomingEvent
import com.pexip.sdk.api.infinity.InfinityEvent
import com.pexip.sdk.api.infinity.LayoutEvent
import com.pexip.sdk.api.infinity.MessageReceivedEvent
import com.pexip.sdk.api.infinity.NewCandidateEvent
import com.pexip.sdk.api.infinity.NewOfferEvent
import com.pexip.sdk.api.infinity.ParticipantCreateEvent
import com.pexip.sdk.api.infinity.ParticipantDeleteEvent
import com.pexip.sdk.api.infinity.ParticipantResponse
import com.pexip.sdk.api.infinity.ParticipantSyncBeginEvent
import com.pexip.sdk.api.infinity.ParticipantSyncEndEvent
import com.pexip.sdk.api.infinity.ParticipantUpdateEvent
import com.pexip.sdk.api.infinity.PeerDisconnectEvent
import com.pexip.sdk.api.infinity.PresentationStartEvent
import com.pexip.sdk.api.infinity.PresentationStopEvent
import com.pexip.sdk.api.infinity.ReferEvent
import com.pexip.sdk.api.infinity.RequestedLayout
import com.pexip.sdk.api.infinity.Screen
import com.pexip.sdk.api.infinity.SpeakerResponse
import com.pexip.sdk.api.infinity.SplashScreenEvent
import com.pexip.sdk.api.infinity.StageEvent
import com.pexip.sdk.api.infinity.UnknownEvent
import com.pexip.sdk.api.infinity.UpdateSdpEvent
import com.pexip.sdk.api.infinity.readUtf8
import com.pexip.sdk.infinity.BreakoutId
import com.pexip.sdk.infinity.LayoutId
import com.pexip.sdk.infinity.ParticipantId
import com.pexip.sdk.infinity.Role
import com.pexip.sdk.infinity.ServiceType
import com.pexip.sdk.infinity.test.nextString
import kotlinx.datetime.Instant
import okio.FileSystem
import kotlin.random.Random
import kotlin.test.Test
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class InfinityEventTest {

    @Test
    fun `returns Event if the type is registered`() {
        tableOf("type", "filename", "event")
            .row<String, String, Event>(
                val1 = "bye",
                val2 = "bye.json",
                val3 = ByeEvent,
            )
            .row(
                val1 = "disconnect",
                val2 = "disconnect.json",
                val3 = DisconnectEvent("Disconnected by remote host"),
            )
            .row(
                val1 = "message_received",
                val2 = "message_received.json",
                val3 = MessageReceivedEvent(
                    participantName = "George",
                    participantId = ParticipantId("dc46269f-5b39-4356-93fd-94d31b890bd5"),
                    type = "text/plain",
                    payload = "hello",
                    direct = true,
                ),
            )
            .row(
                val1 = "presentation_start",
                val2 = "presentation_start.json",
                val3 = PresentationStartEvent(
                    presenterId = ParticipantId("0296f038-7f41-4c73-8dcf-0b95bd0138c7"),
                    presenterName = "George",
                ),
            )
            .row(
                val1 = "presentation_stop",
                val2 = "presentation_stop.json",
                val3 = PresentationStopEvent,
            )
            .row(
                val1 = "incoming",
                val2 = "incoming.json",
                val3 = IncomingEvent(
                    conferenceAlias = "george@example.com",
                    remoteDisplayName = "George",
                    token = "CXU",
                ),
            )
            .row(
                val1 = "incoming_cancelled",
                val2 = "incoming_cancelled.json",
                val3 = IncomingCancelledEvent("CXU"),
            )
            .row(
                val1 = "fecc",
                val2 = "fecc_start.json",
                val3 = FeccEvent(
                    action = FeccAction.START,
                    timeout = 1.seconds,
                    movement = listOf(FeccMovement.PAN_LEFT, FeccMovement.TILT_UP),
                ),
            )
            .row(
                val1 = "fecc",
                val2 = "fecc_continue.json",
                val3 = FeccEvent(
                    action = FeccAction.CONTINUE,
                    timeout = 200.milliseconds,
                    movement = listOf(FeccMovement.ZOOM_IN, FeccMovement.PAN_RIGHT),
                ),
            )
            .row(
                val1 = "fecc",
                val2 = "fecc_stop.json",
                val3 = FeccEvent(
                    action = FeccAction.STOP,
                    timeout = 100.milliseconds,
                    movement = listOf(FeccMovement.TILT_DOWN, FeccMovement.ZOOM_OUT),
                ),
            )
            .row(
                val1 = "fecc",
                val2 = "fecc_unknown.json",
                val3 = FeccEvent(
                    action = FeccAction.UNKNOWN,
                    timeout = Duration.ZERO,
                    movement = List(3) { FeccMovement.UNKNOWN },
                ),
            )
            .row(
                val1 = "refer",
                val2 = "refer.json",
                val3 = ReferEvent(
                    conferenceAlias = "toto",
                    token = "ZqW",
                ),
            )
            .row(
                val1 = "new_offer",
                val2 = "new_offer.json",
                val3 = NewOfferEvent("dDxBW7fN4d"),
            )
            .row(
                val1 = "update_sdp",
                val2 = "update_sdp.json",
                val3 = UpdateSdpEvent("XY4ckQoc2g"),
            )
            .row(
                val1 = "new_candidate",
                val2 = "new_candidate.json",
                val3 = NewCandidateEvent(
                    candidate = "sXyJ0h2vh8",
                    mid = "tyU1tUaHqn",
                    ufrag = "2WrhfEx9Jh",
                    pwd = "EBZ0fhSaJt",
                ),
            )
            .row(
                val1 = "peer_disconnect",
                val2 = "peer_disconnect.json",
                val3 = PeerDisconnectEvent,
            )
            .row(
                val1 = "splash_screen",
                val2 = "splash_screen.json",
                val3 = SplashScreenEvent("direct_media_welcome"),
            )
            .row(
                val1 = "splash_screen",
                val2 = "splash_screen_null.json",
                val3 = SplashScreenEvent(),
            )
            .row(
                val1 = "breakout_begin",
                val2 = "breakout_begin.json",
                val3 = BreakoutBeginEvent(
                    id = BreakoutId("6feed192-5dc0-4c42-abb8-e82558516112"),
                    participantId = ParticipantId("682e9fb0-b1c9-48de-9eea-05cfff875915"),
                ),
            )
            .row(
                val1 = "breakout_end",
                val2 = "breakout_end.json",
                val3 = BreakoutEndEvent(
                    id = BreakoutId("6feed192-5dc0-4c42-abb8-e82558516112"),
                    participantId = ParticipantId("682e9fb0-b1c9-48de-9eea-05cfff875915"),
                ),
            )
            .row(
                val1 = "participant_sync_begin",
                val2 = "participant_sync_begin.json",
                val3 = ParticipantSyncBeginEvent,
            )
            .row(
                val1 = "participant_sync_end",
                val2 = "participant_sync_end.json",
                val3 = ParticipantSyncEndEvent,
            )
            .row(
                val1 = "participant_create",
                val2 = "participant_create.json",
                val3 = ParticipantCreateEvent(
                    response = ParticipantResponse(
                        id = ParticipantId("0296f038-7f41-4c73-8dcf-0b95bd0138c7"),
                        startTime = Instant.fromEpochSeconds(
                            epochSeconds = 1700484383,
                            nanosecondAdjustment = 846972000,
                        ),
                        displayName = "George R",
                        overlayText = "George",
                        audioMuted = true,
                        videoMuted = false,
                        presenting = false,
                        muteSupported = true,
                        transferSupported = true,
                        disconnectSupported = true,
                        role = Role.GUEST,
                        serviceType = ServiceType.CONFERENCE,
                        callTag = "dd806491-eecc-42fc-accb-86a99448fc17",
                        clientAudioMuted = false,
                    ),
                ),
            )
            .row(
                val1 = "participant_create",
                val2 = "participant_create_unknown.json",
                val3 = ParticipantCreateEvent(
                    response = ParticipantResponse(
                        id = ParticipantId("0296f038-7f41-4c73-8dcf-0b95bd0138c7"),
                        startTime = Instant.fromEpochSeconds(
                            epochSeconds = 1700484383,
                            nanosecondAdjustment = 846972000,
                        ),
                        displayName = "George R",
                        overlayText = "George",
                        audioMuted = true,
                        videoMuted = false,
                        presenting = false,
                        muteSupported = true,
                        transferSupported = true,
                        disconnectSupported = true,
                        role = Role.UNKNOWN,
                        serviceType = ServiceType.CONFERENCE,
                        callTag = "dd806491-eecc-42fc-accb-86a99448fc17",
                        clientAudioMuted = false,
                    ),
                ),
            )
            .row(
                val1 = "participant_update",
                val2 = "participant_update.json",
                val3 = ParticipantUpdateEvent(
                    response = ParticipantResponse(
                        id = ParticipantId("0296f038-7f41-4c73-8dcf-0b95bd0138c7"),
                        startTime = Instant.fromEpochSeconds(
                            epochSeconds = 1700486477,
                            nanosecondAdjustment = 114828000,
                        ),
                        buzzTime = Instant.fromEpochSeconds(
                            epochSeconds = 1700486478,
                            nanosecondAdjustment = 98765000,
                        ),
                        spotlightTime = Instant.fromEpochSeconds(
                            epochSeconds = 1700486479,
                            nanosecondAdjustment = 123456000,
                        ),
                        displayName = "George R",
                        overlayText = "George",
                        audioMuted = true,
                        videoMuted = false,
                        presenting = false,
                        muteSupported = true,
                        transferSupported = true,
                        disconnectSupported = true,
                        role = Role.HOST,
                        serviceType = ServiceType.GATEWAY,
                        callTag = "dd806491-eecc-42fc-accb-86a99448fc17",
                        clientAudioMuted = true,
                    ),
                ),
            )
            .row(
                val1 = "participant_delete",
                val2 = "participant_delete.json",
                val3 = ParticipantDeleteEvent(
                    id = ParticipantId("0296f038-7f41-4c73-8dcf-0b95bd0138c7"),
                ),
            )
            .row(
                val1 = "layout",
                val2 = "layout.json",
                val3 = LayoutEvent(
                    layout = LayoutId("1:0"),
                    requestedLayout = RequestedLayout(
                        primaryScreen = Screen(
                            hostLayout = LayoutId("ac"),
                            guestLayout = LayoutId("ac"),
                        ),
                    ),
                    overlayTextEnabled = true,
                ),
            )
            .row(
                val1 = "layout",
                val2 = "layout_direct_media.json",
                val3 = LayoutEvent(layout = LayoutId("1:0")),
            )
            .row(
                val1 = "conference_update",
                val2 = "conference_update.json",
                val3 = ConferenceUpdateEvent(
                    locked = false,
                    started = true,
                    guestsMuted = false,
                    presentationAllowed = true,
                    guestsCanUnmute = false,
                ),
            )
            .row(
                val1 = "stage",
                val2 = "stage.json",
                val3 = StageEvent(
                    SpeakerResponse(
                        participantId = ParticipantId("119895e3-c614-4e92-b906-90fd2afd6dff"),
                        vad = 100,
                    ),
                    SpeakerResponse(
                        participantId = ParticipantId("25206ea5-5fa6-4bfb-93ff-3c6aa74e03c2"),
                        vad = 0,
                    ),
                ),
            )
            .row(
                val1 = "breakout_event",
                val2 = "breakout_event_conference_update.json",
                val3 = BreakoutEvent(
                    id = BreakoutId("04fe9b38-4354-4e87-a4b3-6f589a8dafa3"),
                    event = ConferenceUpdateEvent(
                        locked = false,
                        started = true,
                        guestsMuted = false,
                        presentationAllowed = true,
                        guestsCanUnmute = true,
                    ),
                ),
            )
            .row(
                val1 = "breakout_event",
                val2 = "breakout_event_participant_sync_begin.json",
                val3 = BreakoutEvent(
                    id = BreakoutId("04fe9b38-4354-4e87-a4b3-6f589a8dafa3"),
                    event = ParticipantSyncBeginEvent,
                ),
            )
            .row(
                val1 = "breakout_event",
                val2 = "breakout_event_participant_sync_end.json",
                val3 = BreakoutEvent(
                    id = BreakoutId("04fe9b38-4354-4e87-a4b3-6f589a8dafa3"),
                    event = ParticipantSyncEndEvent,
                ),
            )
            .row(
                val1 = "breakout_event",
                val2 = "breakout_event_participant_create.json",
                val3 = BreakoutEvent(
                    id = BreakoutId("04fe9b38-4354-4e87-a4b3-6f589a8dafa3"),
                    event = ParticipantCreateEvent(
                        response = ParticipantResponse(
                            id = ParticipantId("1e1bdef4-323c-4ed2-8765-1153be63760e"),
                            startTime = Instant.fromEpochSeconds(
                                epochSeconds = 1730272988,
                                nanosecondAdjustment = 677395300,
                            ),
                            buzzTime = null,
                            spotlightTime = null,
                            displayName = "George",
                            overlayText = "George",
                            audioMuted = false,
                            videoMuted = false,
                            presenting = false,
                            muteSupported = true,
                            transferSupported = true,
                            disconnectSupported = true,
                            role = Role.HOST,
                            serviceType = ServiceType.CONFERENCE,
                        ),
                    ),
                ),
            )
            .row(
                val1 = "breakout_event",
                val2 = "breakout_event_participant_update.json",
                val3 = BreakoutEvent(
                    id = BreakoutId("04fe9b38-4354-4e87-a4b3-6f589a8dafa3"),
                    event = ParticipantUpdateEvent(
                        response = ParticipantResponse(
                            id = ParticipantId("c61828bb-01d6-4531-a415-8d5201e190d6"),
                            startTime = Instant.fromEpochSeconds(
                                epochSeconds = 1730272990,
                                nanosecondAdjustment = 897364100,
                            ),
                            buzzTime = null,
                            spotlightTime = null,
                            displayName = "George",
                            overlayText = "George",
                            audioMuted = false,
                            videoMuted = false,
                            presenting = false,
                            muteSupported = true,
                            transferSupported = true,
                            disconnectSupported = true,
                            role = Role.HOST,
                            serviceType = ServiceType.CONFERENCE,
                        ),
                    ),
                ),
            )
            .row(
                val1 = "breakout_event",
                val2 = "breakout_event_participant_delete.json",
                val3 = BreakoutEvent(
                    id = BreakoutId("04fe9b38-4354-4e87-a4b3-6f589a8dafa3"),
                    event = ParticipantDeleteEvent(
                        id = ParticipantId("c61828bb-01d6-4531-a415-8d5201e190d6"),
                    ),
                ),
            )
            .row(
                val1 = "breakout_event",
                val2 = "breakout_event_stage.json",
                val3 = BreakoutEvent(
                    id = BreakoutId("04fe9b38-4354-4e87-a4b3-6f589a8dafa3"),
                    event = StageEvent(
                        SpeakerResponse(
                            participantId = ParticipantId("119895e3-c614-4e92-b906-90fd2afd6dff"),
                            vad = 100,
                        ),
                        SpeakerResponse(
                            participantId = ParticipantId("25206ea5-5fa6-4bfb-93ff-3c6aa74e03c2"),
                            vad = 0,
                        ),
                    ),
                ),
            )
            .forAll { type, filename, event ->
                val data = FileSystem.RESOURCES.readUtf8(filename)
                val actual = InfinityEvent(
                    id = Random.nextString(),
                    type = type,
                    data = data.trim(),
                )
                assertThat(actual, "event").isEqualTo(event)
            }
    }

    @Test
    fun `returns null if the type is not registered`() {
        val actual = InfinityEvent(
            id = Random.nextString(),
            type = Random.nextString(),
            data = Random.nextString(),
        )
        assertThat(actual, "event").isEqualTo(UnknownEvent)
    }
}
