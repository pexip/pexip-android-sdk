package com.pexip.sdk.video.api.internal

import kotlinx.serialization.Serializable

@Serializable
internal class RequestToken403Response(val guest_pin: String) : PinRequirementResponse
