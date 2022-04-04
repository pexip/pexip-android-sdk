package com.pexip.sdk.api.infinity

/**
 * Thrown to indicate that the provided token was invalid.
 */
public class InvalidTokenException @JvmOverloads constructor(message: String? = null) :
    RuntimeException(message)
