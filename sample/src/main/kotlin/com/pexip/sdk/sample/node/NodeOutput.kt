package com.pexip.sdk.sample.node

import java.net.URL

sealed class NodeOutput {

    data class Node(val node: URL) : NodeOutput()

    object Back : NodeOutput() {

        override fun toString(): String = "Back"
    }
}
