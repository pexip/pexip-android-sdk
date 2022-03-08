package com.pexip.sdk.video.coroutines

import com.pexip.sdk.video.Token
import com.pexip.sdk.video.TokenRequest
import com.pexip.sdk.video.TokenRequester
import kotlinx.coroutines.cancelFutureOnCancellation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

public suspend fun TokenRequester.request(request: TokenRequest): Token =
    suspendCancellableCoroutine {
        val callback = object : TokenRequester.Callback {

            override fun onSuccess(requester: TokenRequester, token: Token) {
                it.resume(token)
            }

            override fun onFailure(requester: TokenRequester, t: Throwable) {
                it.resumeWithException(t)
            }
        }
        it.cancelFutureOnCancellation(request(request, callback))
    }
