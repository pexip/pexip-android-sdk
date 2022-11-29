package com.pexip.sdk.api.infinity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public enum class FeccAction {

    @SerialName("start")
    START,

    @SerialName("continue")
    CONTINUE,

    @SerialName("stop")
    STOP,

    UNKNOWN
}
