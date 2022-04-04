package com.pexip.sdk.api.infinity.internal

import com.pexip.sdk.api.infinity.RequestTokenResponse

internal object RequestTokenResponseSerializer :
    UnboxingSerializer<RequestTokenResponse>(RequestTokenResponse.serializer())
