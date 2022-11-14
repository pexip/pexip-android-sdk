package com.pexip.sdk.registration.infinity.internal

import com.pexip.sdk.registration.RegisteredDevice

internal interface RegisteredDevicesFetcher {

    fun fetch(query: String): List<RegisteredDevice>
}
