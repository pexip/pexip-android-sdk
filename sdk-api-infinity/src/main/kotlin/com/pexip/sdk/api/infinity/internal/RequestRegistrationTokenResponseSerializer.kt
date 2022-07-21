package com.pexip.sdk.api.infinity.internal

import com.pexip.sdk.api.infinity.RequestRegistrationTokenResponse

internal object RequestRegistrationTokenResponseSerializer :
    UnboxingSerializer<RequestRegistrationTokenResponse>(RequestRegistrationTokenResponse.serializer())
