package com.pexip.sdk.video.internal

import kotlinx.coroutines.flow.Flow
import kotlin.time.Duration

internal interface InfinityService {

    fun events(): Flow<Event>

    suspend fun refreshToken(): Duration

    suspend fun releaseToken()
}
