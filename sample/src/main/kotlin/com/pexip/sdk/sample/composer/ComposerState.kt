package com.pexip.sdk.sample.composer

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
@JvmInline
value class ComposerState(val message: String = "") : Parcelable {

    val submitEnabled: Boolean
        get() = message.isNotBlank()
}
