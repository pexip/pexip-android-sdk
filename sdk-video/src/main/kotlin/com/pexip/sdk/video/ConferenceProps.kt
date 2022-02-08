package com.pexip.sdk.video

import android.os.Parcelable
import android.util.Patterns
import kotlinx.parcelize.Parcelize

@Parcelize
public class ConferenceProps private constructor(
    internal val alias: String,
    internal val nodeAddress: String,
    internal val displayName: String,
) : Parcelable {

    public class Builder {

        private var alias: String? = null
        private var nodeAddress: String? = null
        private var displayName: String = "Guest"

        public fun alias(alias: String): Builder = apply {
            require(Patterns.EMAIL_ADDRESS.matcher(alias).matches()) {
                "'$alias' is not a valid URI."
            }
            this.alias = alias.trim()
        }

        public fun nodeAddress(nodeAddress: String): Builder = apply {
            require(Patterns.WEB_URL.matcher(nodeAddress).matches()) {
                "'$nodeAddress' is not a valid URL."
            }
            this.nodeAddress = nodeAddress
        }

        public fun displayName(displayName: String): Builder = apply {
            require(displayName.isNotBlank()) { "displayName must not be blank." }
            this.displayName = displayName.trim()
        }

        public fun build(): ConferenceProps = ConferenceProps(
            alias = checkNotNull(alias) { "alias is not set." },
            nodeAddress = checkNotNull(nodeAddress) { "nodeAddress is not set." },
            displayName = displayName
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ConferenceProps) return false
        if (alias != other.alias) return false
        if (nodeAddress != other.nodeAddress) return false
        if (displayName != other.displayName) return false
        return true
    }

    override fun hashCode(): Int {
        var result = alias.hashCode()
        result = 31 * result + nodeAddress.hashCode()
        result = 31 * result + displayName.hashCode()
        return result
    }

    override fun toString(): String =
        "ConferenceProps(alias=$alias, nodeAddress=$nodeAddress, displayName=$displayName)"
}

@JvmSynthetic
public inline fun ConferenceProps(block: ConferenceProps.Builder.() -> Unit): ConferenceProps =
    ConferenceProps.Builder().apply(block).build()
