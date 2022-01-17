package com.pexip.sdk.video

import kotlin.random.Random
import kotlin.random.nextInt

fun Random.nextAlias() = "${nextInt(100000..999999)}"

fun Random.nextPin() = "${nextInt(1000..9999)}"
