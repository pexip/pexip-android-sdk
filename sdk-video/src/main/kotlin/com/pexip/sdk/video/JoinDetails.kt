package com.pexip.sdk.video

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
public class JoinDetails private constructor(
    internal val host: String,
    internal val alias: String,
    internal val displayName: String,
) : Parcelable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is JoinDetails) return false
        if (host != other.host) return false
        if (alias != other.alias) return false
        if (displayName != other.displayName) return false
        return true
    }

    override fun hashCode(): Int {
        var result = host.hashCode()
        result = 31 * result + alias.hashCode()
        result = 31 * result + displayName.hashCode()
        return result
    }

    override fun toString(): String =
        "JoinDetails(host=$host, alias=$alias, displayName=$displayName)"

    public class Builder {

        private var alias: String? = null
        private var host: String? = null
        private var displayName: String? = null

        /**
         * Sets the conference alias
         *
         * @param alias an alias
         * @return this [Builder]
         */
        public fun alias(alias: String): Builder = apply {
            require(alias.isNotBlank()) { "alias is blank." }
            this.alias = alias.trim()
        }

        /**
         * Sets the host
         *
         * @param host a host
         * @return this [Builder]
         */
        public fun host(host: String): Builder = apply {
            require(host.isNotBlank()) { "host is blank." }
            this.host = host.trim()
        }

        /**
         * Sets the preferred display name
         *
         * @param displayName a display name
         * @return this [Builder]
         */
        public fun displayName(displayName: String): Builder = apply {
            require(displayName.isNotBlank()) { "displayName is blank." }
            this.displayName = displayName.trim()
        }

        public fun build(): JoinDetails = JoinDetails(
            host = checkNotNull(host) { "host is not set." },
            alias = checkNotNull(alias) { "alias is not set." },
            displayName = checkNotNull(displayName) { "displayName is not set." }
        )
    }
}
