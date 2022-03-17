package com.pexip.sdk.video.conference.internal

import com.pexip.sdk.video.internal.UnboxingSerializer
import kotlinx.serialization.Serializable

@Serializable
internal data class RefreshToken200Response(val token: String)

internal object RefreshToken200ResponseSerializer :
    UnboxingSerializer<RefreshToken200Response>(RefreshToken200Response.serializer())
