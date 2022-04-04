package com.pexip.sdk.api.infinity.internal

import com.pexip.sdk.api.infinity.RefreshTokenResponse

internal object RefreshTokenResponseSerializer :
    UnboxingSerializer<RefreshTokenResponse>(RefreshTokenResponse.serializer())
