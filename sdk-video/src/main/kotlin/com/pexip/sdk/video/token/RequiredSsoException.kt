package com.pexip.sdk.video.token

/**
 * Thrown to indicate that SSO authentication is required to proceed.
 *
 * @property idps a list of identity providers available for authentication
 */
public class RequiredSsoException(public val idps: List<IdentityProvider>) : RuntimeException()
