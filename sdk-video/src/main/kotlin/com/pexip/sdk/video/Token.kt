package com.pexip.sdk.video

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
public class Token internal constructor(
    internal val node: Node,
    internal val joinDetails: JoinDetails,
    internal val value: String,
) : Parcelable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Token) return false
        if (node != other.node) return false
        if (joinDetails != other.joinDetails) return false
        if (value != other.value) return false
        return true
    }

    override fun hashCode(): Int {
        var result = node.hashCode()
        result = 31 * result + joinDetails.hashCode()
        result = 31 * result + value.hashCode()
        return result
    }

    override fun toString(): String = "Token(node=$node, joinDetails=$joinDetails)"
}
