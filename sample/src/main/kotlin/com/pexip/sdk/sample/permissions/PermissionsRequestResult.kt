package com.pexip.sdk.sample.permissions

data class PermissionsRequestResult(
    val grants: Map<String, Boolean>,
    val rationales: Map<String, Boolean>,
) {

    val allGranted by lazy(LazyThreadSafetyMode.NONE) {
        grants.all { (_, granted) -> granted }
    }
    val anyRationales by lazy(LazyThreadSafetyMode.NONE) {
        rationales.any { (_, rationale) -> rationale }
    }
}
