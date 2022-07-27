package com.pexip.sdk.sample.welcome

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class WelcomeState(
    val displayName: String = "",
    val displayNameToSet: String? = null,
) : Parcelable
