package com.pexip.sdk.api.infinity.internal

import com.pexip.sdk.api.infinity.CallsResponse

internal object CallsResponseSerializer :
    UnboxingSerializer<CallsResponse>(CallsResponse.serializer())
