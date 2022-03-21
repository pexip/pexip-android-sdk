package com.pexip.sdk.video.conference.internal

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable

@OptIn(ExperimentalSerializationApi::class)
@Serializable
internal data class CallsRequest(
    val sdp: String,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val call_type: String = "WEBRTC",
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val present: String = "main",
)
