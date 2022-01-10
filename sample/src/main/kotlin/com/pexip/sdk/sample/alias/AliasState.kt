package com.pexip.sdk.sample.alias

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AliasState(
    val alias: String = "",
    val host: String = "",
    val presentationInMain: Boolean = false,
) : Parcelable
