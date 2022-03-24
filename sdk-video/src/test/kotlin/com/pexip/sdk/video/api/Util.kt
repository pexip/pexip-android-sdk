package com.pexip.sdk.video.api

import com.pexip.sdk.video.nextString
import java.util.UUID
import kotlin.random.Random

internal fun Random.nextConferenceAlias() = ConferenceAlias(nextString(8))

internal fun Random.nextCallId() = CallId(nextUuid())

internal fun Random.nextParticipantId() = ParticipantId(nextUuid())

internal fun Random.nextIdentityProviderId() = IdentityProviderId(nextUuid())

private fun Random.nextUuid() = UUID.randomUUID().toString()
