package com.pexip.sdk.video.api.internal

import com.pexip.sdk.video.api.RefreshTokenResponse

internal object RefreshTokenResponseSerializer :
    UnboxingSerializer<RefreshTokenResponse>(RefreshTokenResponse.serializer())
