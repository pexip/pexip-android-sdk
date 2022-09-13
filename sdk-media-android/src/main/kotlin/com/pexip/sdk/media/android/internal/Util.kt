package com.pexip.sdk.media.android.internal

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import java.io.Closeable
import java.util.SortedMap
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.experimental.ExperimentalTypeInference

internal fun Intent.toPrettyString(): String {
    val action = action
    val extras = extras ?: Bundle.EMPTY
    return buildString {
        append(action)
        append("=")
        with(extras) {
            val s = keySet().joinToString(separator = ",", prefix = "{", postfix = "}") {
                "$it=${get(it)}"
            }
            append(s)
        }
    }
}

@OptIn(ExperimentalTypeInference::class, ExperimentalContracts::class)
internal fun <K : Comparable<K>, V> buildSortedMap(@BuilderInference block: SortedMap<K, V>.() -> Unit): SortedMap<K, V> {
    contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
    return sortedMapOf<K, V>().apply(block)
}

internal inline fun Context.registerReceiver(
    filter: IntentFilter,
    handler: Handler,
    crossinline onReceive: (Context, Intent) -> Unit,
): Closeable = object : BroadcastReceiver(), Closeable {

    init {
        registerReceiver(this, filter, null, handler)
    }

    override fun onReceive(context: Context, intent: Intent) {
        require(filter.hasAction(intent.action))
        onReceive(context, intent)
    }

    override fun close() {
        try {
            unregisterReceiver(this)
        } catch (e: IllegalArgumentException) {
            // noop
        }
    }
}
