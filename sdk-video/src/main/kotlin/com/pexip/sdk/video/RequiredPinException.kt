package com.pexip.sdk.video

/**
 * Thrown to indicate that this conference has a PIN requirement.
 *
 * If the guest PIN is not required, one can join as a guest by providing a blank PIN.
 *
 * @property guestPin true if guest PIN is required, false otherwise
 */
public class RequiredPinException(public val guestPin: Boolean) : RuntimeException()
