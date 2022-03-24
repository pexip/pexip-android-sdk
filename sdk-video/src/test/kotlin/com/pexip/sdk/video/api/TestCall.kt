package com.pexip.sdk.video.api

internal interface TestCall<T> : Call<T> {

    override fun execute(): T = TODO()

    override fun enqueue(callback: Callback<T>): Unit = TODO()

    override fun cancel(): Unit = TODO()
}
