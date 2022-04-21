package com.pexip.sdk.api.infinity

import kotlinx.serialization.Serializable

@Serializable
public data class MessageRequest(val payload: String, val type: String = "text/plain")
