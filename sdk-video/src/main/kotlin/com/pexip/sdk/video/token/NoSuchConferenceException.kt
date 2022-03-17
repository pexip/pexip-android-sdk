package com.pexip.sdk.video.token

/**
 * Thrown to indicate that the conference alias does not exist on the node.
 */
public class NoSuchConferenceException @JvmOverloads constructor(message: String? = null) :
    RuntimeException(message)
