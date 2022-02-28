package com.pexip.sdk.video.internal

import com.pexip.sdk.video.IdentityProvider
import kotlinx.serialization.Serializable

internal sealed interface RequestToken403Response

@Serializable
@JvmInline
internal value class ErrorResponse(val message: String) : RequestToken403Response

@Serializable
internal class RequiredPinResponse(val guest_pin: String) : RequestToken403Response

@Serializable
internal class RequiredSsoResponse(val idp: List<IdentityProvider>) : RequestToken403Response

@Serializable
internal class SsoRedirectResponse(
    val redirect_url: String,
    val redirect_idp: IdentityProvider,
) : RequestToken403Response
