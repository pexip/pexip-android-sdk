package com.pexip.sdk.api.infinity.internal

import com.pexip.sdk.api.infinity.RefreshRegistrationTokenResponse

internal object RefreshRegistrationTokenResponseSerializer :
    UnboxingSerializer<RefreshRegistrationTokenResponse>(RefreshRegistrationTokenResponse.serializer())
