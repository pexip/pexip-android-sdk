package com.pexip.sdk.conference.infinity.internal

import com.pexip.sdk.api.infinity.DtmfRequest
import kotlin.random.Random

private const val CHARACTERS = "_-0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"

internal fun Random.nextString(length: Int) =
    CharArray(length) { CHARACTERS.random(this) }.concatToString()

internal fun Random.nextDigits(length: Int) =
    CharArray(length) { DtmfRequest.ALLOWED_DIGITS.random(this) }.concatToString()
