package com.pexip.sdk.video.api

import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.LongAsStringSerializer

@Serializable
public class Token(
    public val token: String,
    @Serializable(with = LongAsStringSerializer::class)
    public val expires: Long,
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

    override fun toString(): String = "Token(token=██, expires=$expires)"
}
