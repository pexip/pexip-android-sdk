package com.pexip.sdk.sample.permissions

data class PermissionsRendering(
    val permissions: Set<String>,
    val onPermissionsRequestResult: (PermissionsRequestResult) -> Unit,
    val onBackClick: () -> Unit,
)
