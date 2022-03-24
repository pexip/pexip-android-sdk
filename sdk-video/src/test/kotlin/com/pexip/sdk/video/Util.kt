package com.pexip.sdk.video

import kotlin.random.Random

private const val CHARACTERS = "_-0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"

internal fun Random.nextString(length: Int) =
    CharArray(length) { CHARACTERS.random(this) }.concatToString()

internal fun Random.nextToken() = nextString(16)
