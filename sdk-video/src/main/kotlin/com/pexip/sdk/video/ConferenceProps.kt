package com.pexip.sdk.video

import android.os.Parcelable
import android.util.Patterns
import kotlinx.parcelize.Parcelize

@Parcelize
class ConferenceProps private constructor(
    internal val alias: String,
    internal val host: String,
    internal val displayName: String,
) : Parcelable {

    class Builder {

        private var alias: String? = null
        private var displayName: String = "Guest"

        fun alias(alias: String): Builder = apply {
            require(Patterns.EMAIL_ADDRESS.matcher(alias).matches()) {
                "'$alias' is not a valid URI."
            }
            this.alias = alias.trim()
        }

        fun displayName(displayName: String): Builder = apply {
            require(displayName.isNotBlank()) { "displayName must not be blank." }
            this.displayName = displayName.trim()
        }

        fun build(): ConferenceProps {
            val alias = checkNotNull(alias) { "alias is not set." }
            val (_, host) = alias.split("@")
            return ConferenceProps(
                alias = alias,
                host = host,
                displayName = displayName
            )
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ConferenceProps) return false
        if (alias != other.alias) return false
        if (host != other.host) return false
        if (displayName != other.displayName) return false
        return true
    }

    override fun hashCode(): Int {
        var result = alias.hashCode()
        result = 31 * result + host.hashCode()
        result = 31 * result + displayName.hashCode()
        return result
    }

    override fun toString(): String =
        "ConferenceProps(alias=$alias, host=$host, displayName=$displayName)"
}

@JvmSynthetic
inline fun ConferenceProps(block: ConferenceProps.Builder.() -> Unit): ConferenceProps =
    ConferenceProps.Builder().apply(block).build()
