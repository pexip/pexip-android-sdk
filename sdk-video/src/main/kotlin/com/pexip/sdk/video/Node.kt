package com.pexip.sdk.video

import android.os.Parcelable
import com.pexip.sdk.video.internal.HttpUrlParceler
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.TypeParceler
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl

@Parcelize
@TypeParceler<HttpUrl, HttpUrlParceler>
public class Node internal constructor(internal val address: HttpUrl) : Parcelable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Node) return false
        if (address != other.address) return false
        return true
    }

    override fun hashCode(): Int = address.hashCode()

    override fun toString(): String = "Node(address=$address)"

    public companion object {

        @JvmStatic
        @JvmName("get")
        public fun String.toNode(): Node = Node(toHttpUrl())
    }
}
