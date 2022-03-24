package com.pexip.sdk.video.api.internal

import kotlinx.serialization.builtins.serializer

internal object StringSerializer : UnboxingSerializer<String>(String.serializer())
