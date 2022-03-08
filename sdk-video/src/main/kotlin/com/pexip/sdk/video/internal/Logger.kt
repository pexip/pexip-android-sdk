package com.pexip.sdk.video.internal

import android.util.Log

internal interface Logger {

    fun log(message: String)

    companion object : Logger {

        override fun log(message: String) {
            Log.d("Pexip", message)
        }
    }
}
