package com.pexip.sdk.registration.infinity.internal

import com.pexip.sdk.api.infinity.RefreshTokenResponse
import kotlin.random.Random

private const val CHARACTERS = "_-0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"

internal fun Random.nextString(length: Int) =
    CharArray(length) { CHARACTERS.random(this) }.concatToString()

internal fun Random.nextToken() = RefreshTokenResponse(
    token = nextString(8),
    expires = (60L..600L).random(this)
)
