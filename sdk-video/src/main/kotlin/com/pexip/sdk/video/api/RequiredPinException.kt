package com.pexip.sdk.video.api

public class RequiredPinException(public val guestPin: Boolean) : RuntimeException()
