package com.pexip.sdk.video.conference.internal

import com.pexip.sdk.api.Call
import com.pexip.sdk.api.Callback

internal interface TestCall<T> : Call<T> {

    override fun execute(): T = TODO()

    override fun enqueue(callback: Callback<T>): Unit = TODO()

    override fun cancel(): Unit = TODO()
}
