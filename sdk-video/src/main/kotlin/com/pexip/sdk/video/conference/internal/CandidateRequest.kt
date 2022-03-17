package com.pexip.sdk.video.conference.internal

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
internal class CandidateRequest(
    @Transient val callId: String = "",
    val candidate: String,
    val mid: String,
)
