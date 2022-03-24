package com.pexip.sdk.video.api

import kotlinx.serialization.Serializable

@Serializable
public data class NewCandidateRequest(public val candidate: String, public val mid: String)
