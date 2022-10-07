package com.pexip.sdk.sample.preflight

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
@JvmInline
value class PreflightState(val destination: PreflightDestination? = null) : Parcelable
