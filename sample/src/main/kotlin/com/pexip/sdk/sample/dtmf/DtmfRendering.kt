package com.pexip.sdk.sample.dtmf

data class DtmfRendering(
    val onToneClick: (String) -> Unit,
    val onBackClick: () -> Unit
)
