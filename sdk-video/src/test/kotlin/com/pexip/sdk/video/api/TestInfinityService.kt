package com.pexip.sdk.video.api

open class TestInfinityService : InfinityService {

    override suspend fun getPinRequirement(
        nodeAddress: String,
        conferenceAlias: String,
        displayName: String,
    ): PinRequirement = notImplemented()

    override suspend fun requestToken(
        nodeAddress: String,
        conferenceAlias: String,
        displayName: String,
        pin: String,
    ): Token = notImplemented()

    private fun notImplemented(): Nothing = TODO("Not implemented")
}
