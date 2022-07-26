package com.pexip.sdk.api.infinity

/**
 * An Infinity token.
 *
 * @property token an actual token value
 * @property expires a duration in seconds for which this token remains valid
 */
public sealed interface Token {

    public val token: String
    public val expires: Long
}
