package com.pexip.sdk.api.infinity

import com.pexip.sdk.api.infinity.internal.UUIDSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.LongAsStringSerializer
import java.util.UUID

@Serializable
public data class RequestRegistrationTokenResponse(
    public val token: String,
    @Serializable(with = LongAsStringSerializer::class)
    public val expires: Long,
    @Serializable(with = UUIDSerializer::class)
    @SerialName("registration_uuid")
    public val registrationId: UUID,
    @SerialName("directory_enabled")
    public val directoryEnabled: Boolean,
    @SerialName("route_via_registrar")
    public val routeViaRegistrar: Boolean,
)
