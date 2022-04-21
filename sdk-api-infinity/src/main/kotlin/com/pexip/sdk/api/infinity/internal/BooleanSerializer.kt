package com.pexip.sdk.api.infinity.internal

import kotlinx.serialization.builtins.serializer

internal object BooleanSerializer : UnboxingSerializer<Boolean>(Boolean.serializer())
