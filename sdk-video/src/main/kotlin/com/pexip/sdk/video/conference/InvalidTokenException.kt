package com.pexip.sdk.video.conference

/**
 * Thrown to indicate that the provided token was invalid.
 */
public class InvalidTokenException @JvmOverloads constructor(message: String? = null) :
    RuntimeException(message)
