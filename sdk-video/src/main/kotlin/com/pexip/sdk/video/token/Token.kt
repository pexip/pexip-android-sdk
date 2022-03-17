package com.pexip.sdk.video.token

import android.os.Parcelable
import com.pexip.sdk.video.JoinDetails
import com.pexip.sdk.video.node.Node
import com.pexip.sdk.video.token.internal.DurationParceler
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.TypeParceler
import kotlin.time.Duration

@Parcelize
@TypeParceler<Duration, DurationParceler>
public class Token internal constructor(
    internal val node: Node,
    internal val joinDetails: JoinDetails,
    internal val participantId: String,
    internal val token: String,
    internal val expires: Duration,
) : Parcelable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Token) return false
        if (node != other.node) return false
        if (joinDetails != other.joinDetails) return false
        if (participantId != other.participantId) return false
        if (token != other.token) return false
        if (expires != other.expires) return false
        return true
    }

    override fun hashCode(): Int {
        var result = node.hashCode()
        result = 31 * result + joinDetails.hashCode()
        result = 31 * result + participantId.hashCode()
        result = 31 * result + token.hashCode()
        result = 31 * result + expires.hashCode()
        return result
    }

    override fun toString(): String =
        "Token(node=$node, joinDetails=$joinDetails, participantId=$participantId)"
}
