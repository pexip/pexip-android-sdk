package com.pexip.sdk.api.infinity.internal

import com.pexip.sdk.api.infinity.UpdateResponse

internal object UpdateResponseSerializer :
    UnboxingSerializer<UpdateResponse>(UpdateResponse.serializer())
