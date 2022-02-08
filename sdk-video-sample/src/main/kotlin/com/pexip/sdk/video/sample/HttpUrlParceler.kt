package com.pexip.sdk.video.sample

import android.os.Parcel
import kotlinx.parcelize.Parceler
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl

object HttpUrlParceler : Parceler<HttpUrl> {

    override fun create(parcel: Parcel): HttpUrl = parcel.readString()!!.toHttpUrl()

    override fun HttpUrl.write(parcel: Parcel, flags: Int) = parcel.writeString(toString())
}
