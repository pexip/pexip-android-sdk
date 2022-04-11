package com.pexip.sdk.api.infinity.internal

import com.pexip.sdk.api.Call
import com.pexip.sdk.api.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

internal class RealCall<T>(
    client: OkHttpClient,
    request: Request,
    private val mapper: (Response) -> T,
) : Call<T> {

    private val call = client.newCall(request)

    override fun execute(): T = call.execute().use(mapper)

    override fun enqueue(callback: Callback<T>) =
        call.enqueue(RealCallback(this, callback, mapper))

    override fun cancel() = call.cancel()
}
