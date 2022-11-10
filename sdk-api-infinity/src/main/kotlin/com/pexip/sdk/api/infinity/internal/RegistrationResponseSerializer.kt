package com.pexip.sdk.api.infinity.internal

import com.pexip.sdk.api.infinity.RegistrationResponse
import kotlinx.serialization.builtins.ListSerializer

internal object RegistrationResponseSerializer :
    UnboxingSerializer<List<RegistrationResponse>>(ListSerializer(RegistrationResponse.serializer()))
