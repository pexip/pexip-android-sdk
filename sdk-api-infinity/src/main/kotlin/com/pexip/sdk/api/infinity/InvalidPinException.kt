package com.pexip.sdk.api.infinity

/**
 * Thrown to indicate that the provided PIN was invalid.
 */
public class InvalidPinException @JvmOverloads constructor(message: String? = null) :
    RuntimeException(message)
