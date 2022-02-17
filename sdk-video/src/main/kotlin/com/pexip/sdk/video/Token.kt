package com.pexip.sdk.video

import com.pexip.sdk.video.internal.DurationSerializer
import kotlinx.serialization.Serializable
import kotlin.time.Duration

@Serializable
public class Token(
    internal val token: String,
    @Serializable(with = DurationSerializer::class)
    internal val expires: Duration,
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Token) return false
        if (token != other.token) return false
        if (expires != other.expires) return false
        return true
    }

    override fun hashCode(): Int {
        var result = token.hashCode()
        result = 31 * result + expires.hashCode()
        return result
    }

    override fun toString(): String = "Token"
}
