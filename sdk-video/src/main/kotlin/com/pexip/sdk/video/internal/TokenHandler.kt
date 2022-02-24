package com.pexip.sdk.video.internal

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

internal class TokenHandler(private val service: InfinityService) {

    @OptIn(DelicateCoroutinesApi::class)
    fun launchIn(scope: CoroutineScope): Job = scope.launch {
        try {
            while (isActive) {
                val expires = service.refreshToken()
                delay(expires / 2)
            }
        } finally {
            GlobalScope.launch { service.releaseToken() }
        }
    }
}
