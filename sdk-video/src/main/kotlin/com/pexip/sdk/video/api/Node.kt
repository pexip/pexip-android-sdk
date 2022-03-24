package com.pexip.sdk.video.api

import android.os.Parcelable
import com.pexip.sdk.video.api.internal.HttpUrlParceler
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.TypeParceler
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl

@Parcelize
@TypeParceler<HttpUrl, HttpUrlParceler>
@JvmInline
public value class Node internal constructor(internal val address: HttpUrl) : Parcelable {

    public companion object {

        @JvmStatic
        @JvmName("get")
        public fun String.toNode(): Node = Node(toHttpUrl())
    }
}
