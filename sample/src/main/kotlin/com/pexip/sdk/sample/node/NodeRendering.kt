package com.pexip.sdk.sample.node

sealed class NodeRendering {

    object ResolvingNode : NodeRendering() {

        override fun toString(): String = "ResolvingNode"
    }

    data class Failure(val t: Throwable, val onBackClick: () -> Unit) : NodeRendering()
}
