package com.pexip.sdk.api.infinity.internal

import kotlinx.serialization.builtins.serializer

internal object StringSerializer : UnboxingSerializer<String>(String.serializer())
