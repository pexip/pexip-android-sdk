package com.pexip.sdk.video

public class RequiredPinException(public val guestPin: Boolean) : RuntimeException()
