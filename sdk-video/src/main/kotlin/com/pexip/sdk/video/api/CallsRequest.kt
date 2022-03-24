package com.pexip.sdk.video.api

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@OptIn(ExperimentalSerializationApi::class)
@Serializable
public data class CallsRequest(
    public val sdp: String,
    @SerialName("call_type")
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    private val callType: String = "WEBRTC",
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    private val present: String = "main",
)
