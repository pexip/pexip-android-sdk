package com.pexip.sdk.video.api

sealed class PinRequirement {

    class Some(val required: Boolean) : PinRequirement() {

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Some) return false
            if (required != other.required) return false
            return true
        }

        override fun hashCode(): Int = required.hashCode()

        override fun toString(): String = "Some(required=$required)"
    }

    class None(val token: String, val expires: Long) : PinRequirement() {

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is None) return false
            if (token != other.token) return false
            if (expires != other.expires) return false
            return true
        }

        override fun hashCode(): Int {
            var result = token.hashCode()
            result = 31 * result + expires.hashCode()
            return result
        }

        override fun toString(): String = "None(token=$token, expires=$expires)"
    }
}
