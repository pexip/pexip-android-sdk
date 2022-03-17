package com.pexip.sdk.video.conference.internal

import com.pexip.sdk.video.internal.UnboxingSerializer
import kotlinx.serialization.Serializable

@Serializable
internal data class CallsResponse(val call_uuid: String, val sdp: String)

internal object CallsResponseSerializer :
    UnboxingSerializer<CallsResponse>(CallsResponse.serializer())
