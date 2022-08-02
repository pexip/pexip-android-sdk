package com.pexip.sdk.api.infinity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class VersionResponse(
    @SerialName("version_id")
    val versionId: String,
    @SerialName("pseudo_version")
    val pseudoVersion: String,
)
