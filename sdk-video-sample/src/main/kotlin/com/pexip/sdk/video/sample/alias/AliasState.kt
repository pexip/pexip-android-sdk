package com.pexip.sdk.video.sample.alias

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
@JvmInline
value class AliasState(val alias: String = "") : Parcelable
