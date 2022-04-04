package com.pexip.sdk.video.conference.internal

import kotlin.reflect.KProperty

internal operator fun <T> ThreadLocal<T>.getValue(thisRef: Any?, property: KProperty<*>) = get()

internal operator fun <T> ThreadLocal<T>.setValue(thisRef: Any?, property: KProperty<*>, value: T) =
    set(value)
