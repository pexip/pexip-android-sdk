package com.pexip.sdk.registration

/**
 * A registered device.
 *
 * @property alias a unique alias assigned to this device
 * @property description a human-readable description of this device, may be blank
 * @property username a username that this device belongs to, may be blank
 */
public data class RegisteredDevice(
    val alias: String,
    val description: String,
    val username: String,
)
