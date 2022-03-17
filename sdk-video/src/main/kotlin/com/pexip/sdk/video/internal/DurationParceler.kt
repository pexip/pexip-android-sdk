package com.pexip.sdk.video.internal

import android.os.Parcel
import kotlinx.parcelize.Parceler
import kotlin.time.Duration

internal object DurationParceler : Parceler<Duration> {

    override fun create(parcel: Parcel): Duration = Duration.parseIsoString(parcel.readString()!!)

    override fun Duration.write(parcel: Parcel, flags: Int) = parcel.writeString(toIsoString())
}
