package com.pexip.sdk.video.api

import com.pexip.sdk.video.api.internal.OkHttpInfinityService
import okhttp3.OkHttpClient

interface InfinityService {

    suspend fun getPinRequirement(
        nodeAddress: String,
        conferenceAlias: String,
        displayName: String,
    ): PinRequirement

    companion object : InfinityService by OkHttpInfinityService(OkHttpClient())
}
