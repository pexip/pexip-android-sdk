package com.pexip.sdk.api.infinity

/**
 * Thrown to indicate that the client should open the provided URL to proceed with SSO flow.
 *
 * @property url a URL to open
 * @property idp an identity provider used for authentication
 */
public class SsoRedirectException(
    public val url: String,
    public val idp: IdentityProvider,
) : RuntimeException()
