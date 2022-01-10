package com.pexip.sdk.sample.node

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed interface NodeState : Parcelable {

    @Parcelize
    object ResolvingNode : NodeState

    @Parcelize
    @JvmInline
    value class Failure(val t: Throwable) : NodeState
}
