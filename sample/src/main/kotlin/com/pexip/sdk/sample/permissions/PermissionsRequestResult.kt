package com.pexip.sdk.sample.permissions

data class PermissionsRequestResult(
    val grants: Map<String, Boolean>,
    val rationales: Map<String, Boolean>,
)
