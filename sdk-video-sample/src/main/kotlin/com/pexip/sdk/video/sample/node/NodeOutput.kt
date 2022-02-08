package com.pexip.sdk.video.sample.node

import okhttp3.HttpUrl

sealed class NodeOutput {

    data class Node(val address: HttpUrl) : NodeOutput()

    object Back : NodeOutput() {

        override fun toString(): String = "Back"
    }
}
