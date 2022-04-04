package com.pexip.sdk.api.coroutines

import com.pexip.sdk.api.Call
import com.pexip.sdk.api.Callback
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

public suspend fun <T> Call<T>.await(): T = suspendCancellableCoroutine {
    it.invokeOnCancellation { cancel() }
    val callback = object : Callback<T> {

        override fun onSuccess(call: Call<T>, response: T) = it.resume(response)

        override fun onFailure(call: Call<T>, t: Throwable) = it.resumeWithException(t)
    }
    enqueue(callback)
}
