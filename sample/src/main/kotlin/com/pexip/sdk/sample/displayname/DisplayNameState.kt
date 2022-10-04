package com.pexip.sdk.sample.displayname

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class DisplayNameState(
    val displayName: String = "",
    val displayNameToSet: String? = null,
) : Parcelable
