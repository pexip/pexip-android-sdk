package com.pexip.sdk.video.api.internal

import com.pexip.sdk.video.api.CallsResponse

internal object CallsResponseSerializer :
    UnboxingSerializer<CallsResponse>(CallsResponse.serializer())
