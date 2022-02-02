package com.pexip.sdk.video.sample.node

sealed class NodeOutput {

    data class Node(val address: String) : NodeOutput()

    object Back : NodeOutput() {

        override fun toString(): String = "Back"
    }
}
