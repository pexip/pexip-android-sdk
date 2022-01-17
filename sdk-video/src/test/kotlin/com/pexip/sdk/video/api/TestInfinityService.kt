package com.pexip.sdk.video.api

open class TestInfinityService : InfinityService {

    override suspend fun isInMaintenanceMode(nodeAddress: String): Boolean = notImplemented()

    override suspend fun getPinRequirement(
        nodeAddress: String,
        alias: String,
        displayName: String,
    ): PinRequirement = notImplemented()

    override suspend fun requestToken(
        nodeAddress: String,
        alias: String,
        displayName: String,
        pin: String,
    ): Token = notImplemented()

    private fun notImplemented(): Nothing = TODO("Not implemented")
}
