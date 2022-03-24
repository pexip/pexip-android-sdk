package com.pexip.sdk.video.api.internal

import com.pexip.sdk.video.api.Call
import com.pexip.sdk.video.api.Callback
import okhttp3.Response
import java.io.IOException

internal class RealCallback<T>(
    private val call: Call<T>,
    private val callback: Callback<T>,
    private val mapper: (Response) -> T,
) : okhttp3.Callback {

    override fun onResponse(call: okhttp3.Call, response: Response) = try {
        callback.onSuccess(this.call, response.use(mapper))
    } catch (t: Throwable) {
        callback.onFailure(this.call, t)
    }

    override fun onFailure(call: okhttp3.Call, e: IOException) {
        callback.onFailure(this.call, e)
    }
}
