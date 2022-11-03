package com.pexip.sdk.api.infinity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public enum class ServiceType {
    @SerialName("conference") CONFERENCE,
    @SerialName("gateway") GATEWAY,
    @SerialName("test_call") TEST_CALL,
    UNKNOWN
}
