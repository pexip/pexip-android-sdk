package com.pexip.sdk.video.api.internal

import com.pexip.sdk.video.api.RequestTokenResponse

internal object RequestTokenResponseSerializer :
    UnboxingSerializer<RequestTokenResponse>(RequestTokenResponse.serializer())
