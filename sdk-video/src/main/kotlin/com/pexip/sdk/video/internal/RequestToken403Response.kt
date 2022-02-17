package com.pexip.sdk.video.internal

import com.pexip.sdk.video.IdentityProvider
import kotlinx.serialization.Serializable

@Serializable
internal class RequiredPinResponse(val guest_pin: String)

@Serializable
internal class RequiredSsoResponse(val idp: List<IdentityProvider>)

@Serializable
internal class SsoRedirectResponse(val redirect_url: String, val redirect_idp: IdentityProvider)
