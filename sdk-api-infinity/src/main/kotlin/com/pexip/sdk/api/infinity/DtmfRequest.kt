package com.pexip.sdk.api.infinity

import kotlinx.serialization.Serializable

@Serializable
public data class DtmfRequest(val digits: String) {

    init {
        require(digits.trim().all { it in ALLOWED_DIGITS }) { "Illegal digit in '$digits'." }
    }

    public companion object {

        public const val ALLOWED_DIGITS: String = "0123456789*#ABCD"
    }
}
