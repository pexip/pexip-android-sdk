package com.pexip.sdk.media.coroutines

import com.pexip.sdk.media.CapturingListener
import com.pexip.sdk.media.MediaConnection
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

public fun MediaConnection.getMainCapturing(): Flow<Boolean> = getCapturing(
    register = ::registerMainCapturingListener,
    unregister = ::unregisterMainCapturingListener,
)

private inline fun getCapturing(
    crossinline register: (CapturingListener) -> Unit,
    crossinline unregister: (CapturingListener) -> Unit,
) = callbackFlow {
    val listener = object : CapturingListener {

        override fun onCapturing(capturing: Boolean) {
            trySend(capturing)
        }
    }
    register(listener)
    awaitClose { unregister(listener) }
}.distinctUntilChanged()
