package com.pexip.sdk.video.api

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
@JvmInline
public value class ParticipantId(internal val value: String) : Parcelable {

    init {
        require(value.isNotBlank()) { "value is blank." }
    }
}
