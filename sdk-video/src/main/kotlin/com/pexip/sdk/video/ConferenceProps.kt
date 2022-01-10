package com.pexip.sdk.video

import android.os.Parcelable
import android.util.Patterns
import kotlinx.parcelize.Parcelize

@Parcelize
class ConferenceProps private constructor(
    internal val uri: String,
    internal val displayName: String,
) : Parcelable {

    class Builder {

        private var uri: String? = null
        private var displayName: String = "Guest"

        fun uri(uri: String): Builder = apply {
            require(Patterns.EMAIL_ADDRESS.matcher(uri).matches()) { "'$uri' is not a valid URI." }
            this.uri = uri.trim()
        }

        fun displayName(displayName: String): Builder = apply {
            require(displayName.isNotBlank()) { "displayName must not be blank." }
            this.displayName = displayName.trim()
        }

        fun build(): ConferenceProps = ConferenceProps(
            uri = checkNotNull(uri) { "uri is not set." },
            displayName = displayName
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ConferenceProps) return false
        if (uri != other.uri) return false
        if (displayName != other.displayName) return false
        return true
    }

    override fun hashCode(): Int {
        var result = uri.hashCode()
        result = 31 * result + displayName.hashCode()
        return result
    }

    override fun toString(): String = "ConferenceProps(uri=$uri, displayName=$displayName)"
}

@JvmSynthetic
inline fun ConferenceProps(block: ConferenceProps.Builder.() -> Unit): ConferenceProps =
    ConferenceProps.Builder().apply(block).build()
