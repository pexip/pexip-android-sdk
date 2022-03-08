package com.pexip.sdk.video.internal

import android.os.Parcel
import kotlinx.parcelize.Parceler
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import kotlin.time.Duration

internal object HttpUrlParceler : Parceler<HttpUrl> {

    override fun create(parcel: Parcel): HttpUrl = parcel.readString()!!.toHttpUrl()

    override fun HttpUrl.write(parcel: Parcel, flags: Int) = parcel.writeString(toString())
}

internal object DurationParceler : Parceler<Duration> {

    override fun create(parcel: Parcel): Duration = Duration.parseIsoString(parcel.readString()!!)

    override fun Duration.write(parcel: Parcel, flags: Int) = parcel.writeString(toIsoString())
}
