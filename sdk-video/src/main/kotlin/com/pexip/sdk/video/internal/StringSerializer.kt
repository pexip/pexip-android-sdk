package com.pexip.sdk.video.internal

import kotlinx.serialization.builtins.serializer

internal object StringSerializer : UnboxingSerializer<String>(String.serializer())
