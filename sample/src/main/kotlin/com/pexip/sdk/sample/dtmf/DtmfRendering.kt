package com.pexip.sdk.sample.dtmf

data class DtmfRendering(
    val visible: Boolean,
    val onToneClick: (String) -> Unit,
    val onBackClick: () -> Unit,
)
