package com.pexip.sdk.api.infinity

/**
 * Thrown to indicate that the device alias either does not exist on the node or credentials
 * are invalid.
 */
public class NoSuchRegistrationException @JvmOverloads constructor(message: String? = null) :
    RuntimeException(message)
