package com.pexip.sdk.video.internal

import kotlinx.serialization.Serializable

@Serializable
internal data class CallsResponse(val call_uuid: String, val sdp: String)
