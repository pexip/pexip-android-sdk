package com.pexip.sdk.video.sample.node

sealed class NodeOutput {

    data class Node(val node: com.pexip.sdk.video.api.Node) : NodeOutput()

    object Back : NodeOutput() {

        override fun toString(): String = "Back"
    }
}
