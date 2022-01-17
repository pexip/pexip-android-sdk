package com.pexip.sdk.video.api

import com.pexip.sdk.video.api.internal.OkHttpInfinityService

interface InfinityService {

    suspend fun isInMaintenanceMode(nodeAddress: String): Boolean

    suspend fun getPinRequirement(
        nodeAddress: String,
        alias: String,
        displayName: String,
    ): PinRequirement

    suspend fun requestToken(
        nodeAddress: String,
        alias: String,
        displayName: String,
        pin: String,
    ): Token

    companion object : InfinityService by OkHttpInfinityService()
}
