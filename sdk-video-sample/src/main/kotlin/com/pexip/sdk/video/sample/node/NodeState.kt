package com.pexip.sdk.video.sample.node

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed class NodeState : Parcelable {

    @Parcelize
    object ResolvingNode : NodeState()

    @Parcelize
    data class Failure(val t: Throwable) : NodeState()
}
